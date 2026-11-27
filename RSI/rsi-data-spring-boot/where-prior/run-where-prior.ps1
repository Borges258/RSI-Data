param(
    [switch]$EnableLlm,
    [switch]$SkipBuild
)

$ErrorActionPreference = 'Stop'
$labRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..\..')).Path
$dotenvPath = Join-Path $labRoot '.env'
if (-not (Test-Path -LiteralPath $dotenvPath)) { throw "Missing .env at $dotenvPath" }

$dotenv = @{}
foreach ($line in Get-Content -LiteralPath $dotenvPath) {
    $trimmed = $line.Trim()
    if (-not $trimmed -or $trimmed.StartsWith('#') -or -not $trimmed.Contains('=')) { continue }
    $parts = $trimmed.Split('=', 2)
    $key = $parts[0].Trim().ToLowerInvariant()
    $value = $parts[1].Trim().Trim('"').Trim("'")
    if ($key -in @('base_url', 'apikey', 'api_key', 'model')) { $dotenv[$key] = $value }
}

$env:WHERE_PRIOR_BASE_URL = $dotenv['base_url']
$env:WHERE_PRIOR_API_KEY = if ($dotenv.ContainsKey('api_key')) { $dotenv['api_key'] } else { $dotenv['apikey'] }
$env:WHERE_PRIOR_MODEL = $dotenv['model']
$env:WHERE_PRIOR_ALLOW_LLM = if ($EnableLlm) { 'true' } else { 'false' }

$java = (Get-Command java -ErrorAction SilentlyContinue).Source
if (-not $java) { $java = Join-Path $labRoot 'tools\jdk8\bin\java.exe' }
$maven = (Get-Command mvn.cmd -ErrorAction SilentlyContinue).Source
if (-not $maven) { $maven = Join-Path $labRoot '归档\tools\apache-maven-3.9.9\bin\mvn.cmd' }
$jar = Join-Path $PSScriptRoot 'target\where-prior-0.1.0.jar'

if (-not $SkipBuild) {
    $repo = Join-Path $labRoot '.m2\repository'
    & $maven -q -f (Join-Path $PSScriptRoot 'pom.xml') -s (Join-Path $PSScriptRoot 'maven-online-settings.xml') "-Dmaven.repo.local=$repo" test package
    if ($LASTEXITCODE -ne 0) { throw 'WHERE prior build/tests failed' }
}
if (-not (Test-Path -LiteralPath $jar)) { throw "Missing fat jar: $jar" }
Write-Host "Starting WHERE prior service on port 18086; LLM enabled: $EnableLlm"
& $java -jar $jar
