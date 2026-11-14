$ErrorActionPreference = 'Stop'

$mongoComando = Get-Command mongod -ErrorAction SilentlyContinue
$mongoExecutavel = if ($mongoComando) {
    $mongoComando.Source
} else {
    Join-Path $PSScriptRoot '.local\mongodb\bin\mongod.exe'
}

if (!(Test-Path -LiteralPath $mongoExecutavel)) {
    throw 'Instale o MongoDB Community Server e disponibilize mongod no PATH.'
}

function TestarPortaMongo {
    $cliente = [Net.Sockets.TcpClient]::new()
    try {
        $cliente.Connect('127.0.0.1', 27017)
        return $true
    } catch {
        return $false
    } finally {
        $cliente.Dispose()
    }
}

if (TestarPortaMongo) {
    Write-Host 'A porta 27017 ja esta em uso. Se o MongoDB ja estiver iniciado, pode executar o ThinDesk.'
    return
}

$mongoDiretorio = Join-Path $PSScriptRoot '.local\mongodb'
$mongoDados = Join-Path $mongoDiretorio 'dados'
$mongoLog = Join-Path $mongoDiretorio 'mongodb.log'
New-Item -ItemType Directory -Path $mongoDados -Force | Out-Null
$mongoProcesso = Start-Process -FilePath $mongoExecutavel -ArgumentList @(
    '--dbpath', ('"' + $mongoDados + '"'),
    '--logpath', ('"' + $mongoLog + '"'), '--logappend',
    '--bind_ip', '127.0.0.1', '--port', '27017'
) -WindowStyle Hidden -PassThru

for ($tentativa = 0; $tentativa -lt 40; $tentativa++) {
    if ($mongoProcesso.HasExited) {
        throw "O MongoDB encerrou. Consulte o log em $mongoLog"
    }
    if (TestarPortaMongo) {
        Write-Host 'MongoDB local iniciado em mongodb://localhost:27017/thindesk'
        Write-Host "Dados salvos em $mongoDados"
        return
    }
    Start-Sleep -Milliseconds 250
}

throw "O MongoDB nao iniciou no tempo esperado. Consulte o log em $mongoLog"
