Add-Type -AssemblyName System.IO.Compression.FileSystem
$file = Get-ChildItem -Filter "*movibox.docx" | Select-Object -First 1
if (-not $file) {
    Write-Error "File not found"
    exit
}
Write-Output "Reading: $($file.FullName)"
$zip = [IO.Compression.ZipFile]::OpenRead($file.FullName)
foreach ($entry in $zip.Entries) {
    if ($entry.FullName -eq 'word/document.xml') {
        $stream = $entry.Open()
        $reader = New-Object System.IO.StreamReader($stream)
        $text = $reader.ReadToEnd()
        $reader.Close()
        $stream.Close()
        $text = $text -replace '</w:p>', "`n"
        $clean = [System.Text.RegularExpressions.Regex]::Replace($text, '<[^>]+>', '')
        $clean
    }
}
$zip.Dispose()
