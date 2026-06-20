param(
    [Parameter(Mandatory = $true)]
    [string]$FrameworkPackage,

    [Parameter(Mandatory = $true)]
    [string]$BuildToolsDir,

    [string]$Output = (Join-Path $PSScriptRoot 'dist\MelodyGuard-1.0.apk'),
    [string]$Keystore = (Join-Path $PSScriptRoot '.keys\debug.jks'),
    [string]$KeyAlias = 'melodyguard-debug',
    [string]$StorePassword = 'android',
    [string]$KeyPassword = 'android'
)

$ErrorActionPreference = 'Stop'

function Require-File([string]$Path) {
    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        throw "Required file not found: $Path"
    }
}

$aapt2 = Join-Path $BuildToolsDir 'aapt2.exe'
$d8 = Join-Path $BuildToolsDir 'd8.bat'
$zipalign = Join-Path $BuildToolsDir 'zipalign.exe'
$apksigner = Join-Path $BuildToolsDir 'apksigner.bat'

Require-File $FrameworkPackage
Require-File $aapt2
Require-File $d8
Require-File $zipalign
Require-File $apksigner

$javac = (Get-Command javac -ErrorAction Stop).Source
$jar = (Get-Command jar -ErrorAction Stop).Source
$keytool = (Get-Command keytool -ErrorAction Stop).Source

$build = Join-Path $PSScriptRoot 'build'
$stubClasses = Join-Path $build 'stub-classes'
$hookClasses = Join-Path $build 'hook-classes'
$dexDir = Join-Path $build 'dex'

New-Item -ItemType Directory -Force -Path $stubClasses, $hookClasses, $dexDir | Out-Null
New-Item -ItemType Directory -Force -Path (Split-Path -Parent $Output), (Split-Path -Parent $Keystore) | Out-Null

$stubSources = Get-ChildItem -LiteralPath (Join-Path $PSScriptRoot 'compile-stubs') -Recurse -Filter '*.java' -File | ForEach-Object FullName
& $javac --release 8 -encoding UTF-8 -d $stubClasses $stubSources
if ($LASTEXITCODE -ne 0) { throw 'Failed to compile Xposed API stubs.' }

$stubJar = Join-Path $build 'xposed-api-stubs.jar'
& $jar cf $stubJar -C $stubClasses .

$hookSources = Get-ChildItem -LiteralPath (Join-Path $PSScriptRoot 'module\src') -Recurse -Filter '*.java' -File | ForEach-Object FullName
& $javac --release 8 -encoding UTF-8 -cp $stubJar -d $hookClasses $hookSources
if ($LASTEXITCODE -ne 0) { throw 'Failed to compile module sources.' }

$hookJar = Join-Path $build 'hook-classes.jar'
& $jar cf $hookJar -C $hookClasses .

& $d8 --min-api 31 --classpath $stubJar --output $dexDir $hookJar
if ($LASTEXITCODE -ne 0) { throw 'D8 failed.' }

$baseApk = Join-Path $build 'module-base.apk'
& $aapt2 link -I $FrameworkPackage --manifest (Join-Path $PSScriptRoot 'module\AndroidManifest.xml') --min-sdk-version 31 --target-sdk-version 36 --version-code 1 --version-name 1.0.0 -o $baseApk
if ($LASTEXITCODE -ne 0) { throw 'AAPT2 failed.' }

& $jar uf $baseApk -C $dexDir classes.dex -C (Join-Path $PSScriptRoot 'module') assets/xposed_init
if ($LASTEXITCODE -ne 0) { throw 'Failed to assemble APK.' }

$alignedApk = Join-Path $build 'module-aligned.apk'
& $zipalign -p -f 4 $baseApk $alignedApk
if ($LASTEXITCODE -ne 0) { throw 'zipalign failed.' }

if (-not (Test-Path -LiteralPath $Keystore)) {
    & $keytool -genkeypair -noprompt -keystore $Keystore -storepass $StorePassword -keypass $KeyPassword -alias $KeyAlias -keyalg RSA -keysize 2048 -validity 10000 -dname 'CN=MelodyGuard Debug, OU=Development, O=Local, C=CN' | Out-Null
    if ($LASTEXITCODE -ne 0) { throw 'Failed to generate debug keystore.' }
}

& $apksigner sign --ks $Keystore --ks-key-alias $KeyAlias --ks-pass "pass:$StorePassword" --key-pass "pass:$KeyPassword" --v2-signing-enabled true --v3-signing-enabled true --out $Output $alignedApk
if ($LASTEXITCODE -ne 0) { throw 'APK signing failed.' }

& $apksigner verify --verbose $Output
if ($LASTEXITCODE -ne 0) { throw 'APK signature verification failed.' }

Write-Output "Built: $Output"
