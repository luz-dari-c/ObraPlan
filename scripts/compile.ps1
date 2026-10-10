$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $projectRoot
$javaFiles = Get-ChildItem -Path 'src/main/java' -Filter '*.java' -Recurse | ForEach-Object FullName
$classDirectory = [IO.Path]::GetFullPath((Join-Path $projectRoot 'build/classes'))
$expectedDirectory = [IO.Path]::GetFullPath($projectRoot).TrimEnd('\') + '\build\classes'
if ($classDirectory -ne $expectedDirectory) { throw 'La carpeta de compilación no está dentro del proyecto.' }
if (Test-Path -LiteralPath $classDirectory) { Remove-Item -LiteralPath $classDirectory -Recurse -Force }
New-Item -ItemType Directory -Force -Path $classDirectory | Out-Null
& javac --add-modules jdk.httpserver -encoding UTF-8 -d 'build/classes' $javaFiles
if ($LASTEXITCODE -ne 0) { throw 'La compilación falló.' }
