param(
    [ValidateSet("start", "stop", "restart", "status", "ps", "logs", "build", "down")]
    [string]$Action = "status",
    [ValidateSet("infra", "minimal", "core", "extended", "all", "full")]
    [string]$Target = "minimal",
    [string]$Service = "brain-server"
)

$RootDir = Split-Path -Parent $PSScriptRoot
$ComposeFile = Join-Path $RootDir "docker-compose.yml"
$EnvFile = Join-Path $RootDir ".env"

if (-not (Test-Path $EnvFile)) {
    Write-Error ".env not found. Copy .env.example to .env and fill real values first."
    exit 1
}

$infraServices = @("mysql", "redis", "minio")
$coreServices = @("brain-server", "nginx-web")
$extendedServices = @("brain-monitor-admin", "brain-snailjob-server")

function Get-ServicesByTarget([string]$Name) {
    switch ($Name) {
        "infra" { return $infraServices }
        "core" { return $infraServices + $coreServices }
        "minimal" { return $infraServices + $coreServices }
        "extended" { return $infraServices + $coreServices + $extendedServices }
        "all" { return $infraServices + $coreServices + $extendedServices }
        "full" { return $infraServices + $coreServices + $extendedServices }
        default { throw "Unknown target: $Name" }
    }
}

function Invoke-Compose([string[]]$Arguments) {
    & docker compose --env-file $EnvFile -f $ComposeFile @Arguments
}

$services = Get-ServicesByTarget $Target

switch ($Action) {
    "start"   { Invoke-Compose (@("up", "-d") + $services) }
    "stop"    { Invoke-Compose (@("stop") + $services) }
    "restart" { Invoke-Compose (@("restart") + $services) }
    "build"   { Invoke-Compose (@("build") + $services) }
    "status"  { Invoke-Compose @("ps") }
    "ps"      { Invoke-Compose @("ps") }
    "logs"    { Invoke-Compose @("logs", "-f", "--tail=200", $Service) }
    "down"    { Invoke-Compose @("down") }
}
