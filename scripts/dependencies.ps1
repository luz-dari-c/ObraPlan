$ErrorActionPreference = 'Stop'
$destination = Join-Path (Split-Path -Parent $PSScriptRoot) 'build/lib/postgresql.jar'
New-Item -ItemType Directory -Force (Split-Path -Parent $destination) | Out-Null
$expectedHash = '2A32A9DCBC42D67A50AD3A0DE5EFD102C8D2BE46720045F2CBD6689F160AB7C7'
if (-not (Test-Path -LiteralPath $destination)) {
    Invoke-WebRequest 'https://repo.maven.apache.org/maven2/org/postgresql/postgresql/42.7.8/postgresql-42.7.8.jar' -OutFile $destination
}
if ((Get-FileHash -LiteralPath $destination -Algorithm SHA256).Hash -ne $expectedHash) {
    throw 'El controlador PostgreSQL no coincide con el SHA-256 esperado.'
}
