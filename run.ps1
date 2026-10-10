$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot
$settings = Get-Content -Raw 'config/integrations.json' | ConvertFrom-Json
$credentialPath = Join-Path $projectRoot 'config/gemini-key.local.xml'
if (-not $env:GEMINI_API_KEY -and (Test-Path -LiteralPath $credentialPath)) {
    $secret = Import-Clixml -LiteralPath $credentialPath
    $env:GEMINI_API_KEY = [System.Net.NetworkCredential]::new('', $secret).Password
}
if (-not (Test-Path Env:OBRAPLAN_VAULT)) { $env:OBRAPLAN_VAULT = $settings.vaultPath }
$port = if ($env:OBRAPLAN_PORT) { $env:OBRAPLAN_PORT } else { '8080' }
Write-Host 'Compilando ObraPlan...'
& './scripts/compile.ps1'
if ($env:OBRAPLAN_VAULT -and (Test-Path -LiteralPath $settings.python)) {
    & $settings.python 'scripts/sync_knowledge.py' --vault $env:OBRAPLAN_VAULT
    if ($LASTEXITCODE -ne 0) { Write-Warning 'La documentación queda pendiente de sincronizar. La aplicación mostrará el estado del grafo.' }
}
Write-Host "Iniciando ObraPlan en http://127.0.0.1:$port (Ctrl+C para detenerlo)."
& java --add-modules jdk.httpserver -cp 'build/classes' co.obraplan.Server
