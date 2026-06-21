$ErrorActionPreference = "Stop"
$src = "C:\Users\YU\Desktop\Computing_Platform_frontend\Computing_Platform\ppt\week16_build\render.pptx"
$outDir = "C:\Users\YU\Desktop\Computing_Platform_frontend\Computing_Platform\ppt\week16_build\slides"
if (Test-Path $outDir) { Remove-Item $outDir -Recurse -Force }
New-Item -ItemType Directory -Path $outDir | Out-Null

$ppt = New-Object -ComObject PowerPoint.Application
$pres = $ppt.Presentations.Open($src, $true, $false, $false)
$count = $pres.Slides.Count
Write-Host "Slides: $count"
for ($i = 1; $i -le $count; $i++) {
  $num = "{0:D2}" -f $i
  $path = Join-Path $outDir ("slide-" + $num + ".png")
  $pres.Slides.Item($i).Export($path, "PNG", 1600, 900)
}
$pres.Close()
$ppt.Quit()
[System.Runtime.Interopservices.Marshal]::ReleaseComObject($pres) | Out-Null
[System.Runtime.Interopservices.Marshal]::ReleaseComObject($ppt) | Out-Null
Write-Host "DONE"