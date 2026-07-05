param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$OutputDir = "downloads",
    [string[]]$Only = @()
)

$ErrorActionPreference = "Stop"

$targets = @(
    @{ Name = "pdf-version-1.0"; Path = "/api/pdf/version/1.0" },
    @{ Name = "pdf-version-1.1"; Path = "/api/pdf/version/1.1" },
    @{ Name = "pdf-version-1.2"; Path = "/api/pdf/version/1.2" },
    @{ Name = "pdf-version-1.3"; Path = "/api/pdf/version/1.3" },
    @{ Name = "pdf-version-1.4"; Path = "/api/pdf/version/1.4" },
    @{ Name = "pdf-version-1.5"; Path = "/api/pdf/version/1.5" },
    @{ Name = "pdf-version-1.6"; Path = "/api/pdf/version/1.6" },
    @{ Name = "pdf-version-1.7"; Path = "/api/pdf/version/1.7" },
    @{ Name = "pdf-version-2.0"; Path = "/api/pdf/version/2.0" },
    @{ Name = "pdf-a-1a"; Path = "/api/pdf/standard/pdf-a-1a" },
    @{ Name = "pdf-a-1b"; Path = "/api/pdf/standard/pdf-a-1b" },
    @{ Name = "pdf-a-2a"; Path = "/api/pdf/standard/pdf-a-2a" },
    @{ Name = "pdf-a-2b"; Path = "/api/pdf/standard/pdf-a-2b" },
    @{ Name = "pdf-a-2u"; Path = "/api/pdf/standard/pdf-a-2u" },
    @{ Name = "pdf-a-3a"; Path = "/api/pdf/standard/pdf-a-3a" },
    @{ Name = "pdf-a-3b"; Path = "/api/pdf/standard/pdf-a-3b" },
    @{ Name = "pdf-a-3u"; Path = "/api/pdf/standard/pdf-a-3u" },
    @{ Name = "pdf-a-4"; Path = "/api/pdf/standard/pdf-a-4" },
    @{ Name = "pdf-a-4e"; Path = "/api/pdf/standard/pdf-a-4e" },
    @{ Name = "pdf-a-4f"; Path = "/api/pdf/standard/pdf-a-4f" },
    @{ Name = "pdf-x-1a-2001"; Path = "/api/pdf/standard/pdf-x-1a-2001" },
    @{ Name = "pdf-x-3-2002"; Path = "/api/pdf/standard/pdf-x-3-2002" },
    @{ Name = "pdf-x-4"; Path = "/api/pdf/standard/pdf-x-4" },
    @{ Name = "pdf-x-4p"; Path = "/api/pdf/standard/pdf-x-4p" },
    @{ Name = "pdf-x-5g"; Path = "/api/pdf/standard/pdf-x-5g" },
    @{ Name = "pdf-x-5n"; Path = "/api/pdf/standard/pdf-x-5n" },
    @{ Name = "pdf-x-5pg"; Path = "/api/pdf/standard/pdf-x-5pg" },
    @{ Name = "pdf-ua-1"; Path = "/api/pdf/standard/pdf-ua-1" },
    @{ Name = "pdf-ua-2"; Path = "/api/pdf/standard/pdf-ua-2" },
    @{ Name = "pdf-vt-1"; Path = "/api/pdf/standard/pdf-vt-1" },
    @{ Name = "pdf-vt-2"; Path = "/api/pdf/standard/pdf-vt-2" }
)

if ($Only.Count -gt 0) {
    $lookup = @{}
    foreach ($name in $Only) {
        $lookup[$name.ToLowerInvariant()] = $true
    }

    $targets = @($targets | Where-Object {
        $lookup.ContainsKey($_.Name.ToLowerInvariant())
    })

    if ($targets.Count -eq 0) {
        throw "No matching targets found for -Only: $($Only -join ', ')"
    }
}

if (-not (Test-Path -Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir | Out-Null
}

Write-Host "Downloading $($targets.Count) PDF files from $BaseUrl to $OutputDir"

foreach ($target in $targets) {
    $uri = "$BaseUrl$($target.Path)"
    $filePath = Join-Path $OutputDir "$($target.Name).pdf"

    try {
        Invoke-WebRequest -Uri $uri -OutFile $filePath
        Write-Host "OK   $($target.Name) -> $filePath"
    } catch {
        Write-Warning "FAIL $($target.Name) ($uri): $($_.Exception.Message)"
    }
}

Write-Host "Done."
