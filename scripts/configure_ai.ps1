$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$credentialPath = Join-Path $projectRoot 'config/gemini-key.local.xml'
Write-Host 'Crea una clave en https://aistudio.google.com/api-keys para un proyecto SIN facturación habilitada.'
Write-Host 'La API tiene cuotas gratuitas. ObraPlan no puede comprobar el estado de facturación de Google.'
Write-Host 'Google puede usar el contenido del nivel gratuito para mejorar sus productos.'
$freeProject = Read-Host '¿Confirmas que esta clave pertenece a un proyecto sin facturación? Escribe SI'
if ($freeProject.Trim().ToUpperInvariant() -ne 'SI') { throw 'Configuración cancelada; no se guardó ninguna clave.' }
$secret = Read-Host 'Clave Gemini (entrada oculta; no la pegues en el chat)' -AsSecureString
if ($secret.Length -lt 10) { throw 'La clave está vacía o parece incompleta.' }
$secret | Export-Clixml -LiteralPath $credentialPath
Write-Host 'Clave guardada con protección de Windows para tu usuario. Reinicia ObraPlan con ./run.ps1.'
