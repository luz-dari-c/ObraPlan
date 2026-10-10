$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $projectRoot
& './scripts/compile.ps1'
$testFiles = Get-ChildItem src/main/java,src/test/java -Filter '*.java' -Recurse | ForEach-Object FullName
New-Item -ItemType Directory -Force build/test-classes | Out-Null
& javac --add-modules jdk.httpserver -encoding UTF-8 -d build/test-classes $testFiles
if ($LASTEXITCODE -ne 0) { throw 'No se pudieron compilar las verificaciones.' }
& java --add-modules jdk.httpserver -cp build/test-classes co.obraplan.Verification $projectRoot
if ($LASTEXITCODE -ne 0) { throw 'Falló la verificación Java.' }
& java --add-modules jdk.httpserver -cp build/test-classes co.obraplan.AgentVerification $projectRoot
if ($LASTEXITCODE -ne 0) { throw 'Falló la verificación de agentes IA.' }
$settings = Get-Content -Raw 'config/integrations.json' | ConvertFrom-Json
$projectPython = if (Test-Path -LiteralPath $settings.python) { $settings.python } else { 'python' }
& $projectPython scripts/verify_knowledge.py
if ($LASTEXITCODE -ne 0) { throw 'Falló la verificación del conocimiento.' }
& $projectPython scripts/verify_ai_http.py
if ($LASTEXITCODE -ne 0) { throw 'Falló la verificación HTTP de agentes.' }
Write-Host 'Verificación completa. Evidencia en output/verification/.'
