# PowerShell script pour démarrer le projet HR Management avec une seule commande.
# Usage : .\start.ps1

[CmdletBinding()]
param(
    [string]$JavaHome = $env:JAVA_HOME
)

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
Set-Location $scriptDir

function Throw-Message([string]$message) {
    Write-Host $message -ForegroundColor Red
    exit 1
}

if (-not $JavaHome) {
    $candidates = @(
        'C:\Program Files\Java\jdk-17',
        'C:\Program Files\Java\jdk-25',
        'C:\Program Files\Java\jdk-11',
        'C:\Program Files (x86)\Java\jdk-17',
        'C:\Program Files (x86)\Java\jdk-11'
    )
    $JavaHome = $candidates | ForEach-Object {
        if (Test-Path $_) { $_ }
    } | Select-Object -First 1
}

if (-not $JavaHome) {
    Throw-Message "JAVA_HOME est introuvable. Définissez la variable d'environnement JAVA_HOME ou lancez le script avec -JavaHome 'C:\Path\to\jdk'."
}

if (-not (Test-Path $JavaHome)) {
    Throw-Message "JAVA_HOME est défini sur '$JavaHome', mais ce dossier n'existe pas."
}

$javaExe = Join-Path $JavaHome 'bin\java.exe'
if (-not (Test-Path $javaExe)) {
    Throw-Message "Aucun java.exe trouvé dans '$JavaHome\bin'. Vérifiez votre installation du JDK."
}

$env:JAVA_HOME = $JavaHome
$env:Path = "$($JavaHome)\bin;$env:Path"
Write-Host "JAVA_HOME utilisé : $env:JAVA_HOME"

$wrapper = Join-Path $scriptDir 'mvnw.cmd'
if (-not (Test-Path $wrapper)) {
    Throw-Message "Impossible de trouver mvnw.cmd. Assurez-vous d'exécuter ce script depuis la racine du projet."
}

Write-Host 'Lancement du projet avec Maven Wrapper...'
& $wrapper clean compile exec:java
exit $LASTEXITCODE
