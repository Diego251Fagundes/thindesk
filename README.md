# ThinDesk

Sistema acadêmico de controle de chamados técnicos, desenvolvido em Java com Spring Boot, Thymeleaf e MongoDB, a partir do projeto base apresentado em sala.

## Acesso à aplicação

- **Aplicação:** [ThinDesk no Render](https://thindesk.onrender.com).
- **Código-fonte:** [repositório individual no GitHub](https://github.com/Diego251Fagundes/thindesk).

Credenciais configuradas para a demonstração:

| Perfil | Usuário | Senha |
| --- | --- | --- |
| Solicitante | `admin` | `admin123` |
| Técnico | `tecnico` | `tecnico123` |

Qualquer pessoa pode utilizar essas contas para avaliar o sistema. O solicitante abre e acompanha seus chamados; o técnico consulta todos os chamados e registra as soluções. Para trocar de perfil, clique em **Sair**.

**Validação online realizada em 14/11/2026:** login dos dois perfis, abertura e resolução de chamados, persistência no Atlas, permissões, filtros de 30/60/90 dias e logout.

## Versão e versionamento

**Versão atual: `1.0.0-beta`**, definida no `pom.xml`.

O projeto adota o formato de versionamento semântico `MAJOR.MINOR.PATCH`, com um sufixo opcional de pré-lançamento:

| Parte | Significado | Exemplo de evolução |
| --- | --- | --- |
| `MAJOR` | Mudanças incompatíveis com a versão anterior. | `1.0.0` → `2.0.0` |
| `MINOR` | Novas funcionalidades compatíveis com a versão atual. | `1.0.0` → `1.1.0` |
| `PATCH` | Correções compatíveis, sem acrescentar funcionalidades. | `1.0.0` → `1.0.1` |
| `-beta` | Identifica uma versão de pré-lançamento, ainda em validação. | `1.0.0-beta` → `1.0.0` |

Em `1.0.0-beta`, o `1` identifica a primeira versão principal; os dois zeros indicam que ainda não houve incremento de funcionalidades ou correções dentro dessa versão. O sufixo `beta` identifica a etapa de validação da versão acadêmica. O pacote gerado pelo Maven é `target/thindesk-1.0.0-beta.war`.

## Funcionalidades

- Login com perfis de solicitante e técnico, mensagem de credenciais inválidas e botão **Sair** para encerrar a sessão.
- Abertura de chamados com título e descrição, registrando solicitante, data de abertura e status `ABERTO` automaticamente.
- Consulta dos próprios chamados pelo solicitante e de todos os chamados pelo técnico, pela interface web.
- Resolução pelo técnico, com solução, responsável, data de resolução e status `RESOLVIDO`.
- Validação dos campos obrigatórios e bloqueio de novas resoluções em chamados já resolvidos.
- Dashboard com indicadores, gráficos, chamados recentes e ranking de técnicos por menor tempo médio de resolução.
- Filtros de 30, 60 e 90 dias e persistência dos dados no MongoDB.

## Dashboard

Print real da aplicação executada localmente, com chamados de demonstração:

![Dashboard do ThinDesk com indicadores, gráficos, ranking de técnicos e filtros de período](docs/images/dashboard.png)

O filtro seleciona os chamados pela **data de abertura**, incluindo o dia atual e usando o fuso `America/Sao_Paulo`. O solicitante vê os indicadores dos próprios chamados; o técnico vê os de todos os chamados.

- Os números mostram o total selecionado e os status atuais de abertos e resolvidos.
- O gráfico mostra as aberturas e as resoluções diárias dos chamados selecionados.
- O tempo médio considera o intervalo entre abertura e resolução, apenas quando as duas datas são válidas.
- O ranking exibe até cinco técnicos, do menor para o maior tempo médio, com a quantidade de resoluções considerada.
- Chamados sem data de abertura não entram nos indicadores por período. Na ausência de dados, a tela apresenta os estados vazios correspondentes.

Os valores do print refletem o banco usado na demonstração. Uma instalação com banco vazio começa sem chamados.

## Pré-requisitos

- JDK 23 configurado no `JAVA_HOME` e no `PATH`.
- Maven disponível no terminal.
- MongoDB Community Server local, com `mongod` no `PATH`, ou uma conexão com MongoDB Atlas.
- Navegador para usar a interface. Postman ou Insomnia são opcionais para testar a API.

## Executar localmente com MongoDB local

Na raiz do projeto, abra o PowerShell e execute:

```powershell
powershell -ExecutionPolicy Bypass -File .\iniciar-mongodb.ps1

$env:THINDESK_MONGODB_URI = "mongodb://localhost:27017/thindesk"
$env:THINDESK_PASSWORD = "admin123"
$env:THINDESK_TECNICO_PASSWORD = "tecnico123"

mvn package spring-boot:repackage
java -jar target/thindesk-1.0.0-beta.war
```

O script inicia o MongoDB local, caso a porta 27017 não esteja em uso, e salva os dados em `.local/mongodb/dados`, fora do Git. O executável do MongoDB precisa estar instalado; o script não o baixa.

A aplicação exige que as três variáveis de ambiente estejam definidas antes da inicialização. As senhas acima são exemplos para demonstração; o código lê os valores configurados no ambiente.

Acesse o [login local](http://localhost:8080/login). Depois de entrar, a aplicação abre o [dashboard](http://localhost:8080/).

### Credenciais de demonstração

Ao usar os valores do comando anterior:

| Perfil | Usuário | Senha | Acesso pela interface |
| --- | --- | --- | --- |
| Solicitante | `admin` | `admin123` | Abrir chamados, acompanhar os próprios chamados e consultar suas soluções. |
| Técnico | `tecnico` | `tecnico123` | Consultar todos os chamados e registrar soluções. |

Apesar do nome `admin`, esse usuário possui o papel de **solicitante** nesta implementação.

### Validar o fluxo pelo navegador

1. Entre como `admin` e acesse **Chamados → Novo chamado**.
2. Preencha título e descrição e clique em **Abrir chamado**. Confira a confirmação e o status `ABERTO` na listagem.
3. Clique em **Sair** no menu lateral e entre como `tecnico` para trocar de perfil.
4. Acesse **Chamados → Ver detalhes**, informe a solução e clique em **Resolver chamado**.
5. Confira o status `RESOLVIDO`, o técnico, a solução e a data de resolução. O formulário de resolução deixa de ser exibido.
6. Clique em **Sair**, entre novamente como `admin` e consulte a solução em **Ver detalhes**. Confira também os indicadores e filtros no dashboard.

Para encerrar a aplicação iniciada no terminal, pressione `Ctrl+C`.

## Executar com MongoDB Atlas

Configure no Atlas um usuário de banco com acesso de leitura e escrita ao banco `thindesk` e autorize o IP do computador. No PowerShell, substitua a conexão local pela conexão do seu cluster:

```powershell
$env:THINDESK_MONGODB_URI = "mongodb+srv://USUARIO:SENHA@SEU_CLUSTER/thindesk?retryWrites=true&w=majority&authSource=admin"
$env:THINDESK_PASSWORD = "admin123"
$env:THINDESK_TECNICO_PASSWORD = "tecnico123"

mvn package spring-boot:repackage
java -jar target/thindesk-1.0.0-beta.war
```

A senha do banco é diferente das senhas de login da aplicação. Se ela contiver caracteres especiais, codifique-os para uso em URL. Mantenha a conexão real fora do Git. Caso a conexão `mongodb+srv` apresente erro de DNS, utilize a conexão alternativa completa fornecida pelo Atlas, com TLS/SSL habilitado.

O banco e as coleções são criados pela aplicação ao iniciar com uma conexão autorizada. Os chamados ficam na coleção `chamados` do banco `thindesk`.

## Executar pelo IntelliJ

1. Abra a pasta do projeto e importe o `pom.xml` como projeto Maven.
2. Selecione o JDK 23 como SDK do projeto.
3. Inicie o MongoDB local ou confirme que a conexão com o Atlas está autorizada.
4. Crie uma configuração **Application** com a classe principal `com.pfc.ThindeskApplication` e o módulo do projeto.
5. Configure as variáveis `THINDESK_MONGODB_URI`, `THINDESK_PASSWORD` e `THINDESK_TECNICO_PASSWORD` com os mesmos valores da execução desejada e execute a aplicação.

As configurações de `.idea` e os arquivos de `.local` não são enviados ao GitHub.

## Testes

```powershell
mvn test
```

Os testes automatizados do dashboard verificam os limites dos períodos no fuso local, os filtros de 30/60/90 dias, a ordenação do ranking por média, as datas inválidas, os estados sem dados e a distribuição dos eventos no gráfico.

## API de chamados

O login utiliza sessão e a proteção CSRF está habilitada, inclusive na API. Os formulários do navegador enviam o token automaticamente.

Para autenticar no Postman ou Insomnia:

1. Envie `GET http://localhost:8080/login`, mantenha o cookie `JSESSIONID` e copie o valor do campo oculto `_csrf` no HTML retornado.
2. Envie `POST /login` com corpo `x-www-form-urlencoded`, incluindo `username`, `password` e `_csrf`, usando as credenciais configuradas no ambiente e a mesma sessão.
3. Após o login, atualize o cookie caso ele tenha mudado e envie `GET /`. Copie o novo token `_csrf` do formulário **Sair**, pois o token é renovado na autenticação.
4. Mantenha a sessão nas próximas requisições. Em `POST`, `PUT` e `DELETE`, envie o token no cabeçalho `X-CSRF-TOKEN`. Para sair, envie `POST /logout` com esse cabeçalho.

Requisições que alteram dados sem um token CSRF válido retornam `403`. Depois de sair, obtenha uma nova sessão e um novo token na página de login antes de autenticar novamente.

Todas as rotas abaixo exigem autenticação. Nos corpos JSON, utilize `Content-Type: application/json`.

| Método | Rota | Funcionalidade |
| --- | --- | --- |
| POST | `/api/chamados` | Abrir um chamado. |
| GET | `/api/chamados` | Listar os chamados do próprio usuário. |
| GET | `/api/chamados/usuario/{id}` | Listar os chamados do próprio usuário, identificado pelo login. |
| GET | `/api/chamados/{id}` | Consultar um chamado do próprio usuário. |
| PUT | `/api/chamados/{id}/resolver` | Resolver um chamado, com acesso exclusivo do técnico. |

Na rota `/usuario/{id}`, o identificador é o nome de login, por exemplo `admin`. Nas demais rotas, é o identificador do chamado retornado pela API. A consulta da API continua restrita aos próprios chamados, inclusive no perfil técnico; a visão de todos os chamados para atendimento está disponível na interface web.

Exemplo de abertura:

```json
{
  "titulo": "Impressora sem funcionar",
  "descricao": "A impressora não imprime os documentos."
}
```

Exemplo de resolução:

```json
{
  "solucao": "Driver da impressora reinstalado."
}
```

Na API, a abertura retorna `201`; campos obrigatórios inválidos retornam `400`, acesso sem permissão retorna `403`, chamado inexistente retorna `404` e uma nova tentativa de resolver um chamado já resolvido retorna `409`. Nas telas, as validações de formulário são apresentadas como mensagens para o usuário.

## Deploy

O serviço [ThinDesk](https://thindesk.onrender.com) está configurado no **Render**, como um **Web Service** com runtime **Docker** e plano **Free**, utilizando o MongoDB Atlas para persistir os chamados.

- O `Dockerfile` compila e testa a aplicação com Maven e Java 23. A imagem final executa apenas o pacote, com usuário sem privilégios de administrador.
- O arquivo `.dockerignore` limita os arquivos enviados para a compilação, excluindo configurações locais e arquivos privados.
- O perfil `render`, ativado pelo contêiner, utiliza a variável `PORT` do serviço, aceita conexões em `0.0.0.0`, reconhece os cabeçalhos HTTPS do proxy e marca o cookie de sessão como seguro para acesso HTTPS.

O serviço utiliza o repositório deste projeto, a branch `main` e o `Dockerfile` da raiz. As variáveis abaixo são configuradas no ambiente do Render:

| Variável | Valor |
| --- | --- |
| `THINDESK_MONGODB_URI` | Conexão privada do MongoDB Atlas, com acesso ao banco `thindesk`. |
| `THINDESK_PASSWORD` | Senha do solicitante `admin`; para demonstração, `admin123`. |
| `THINDESK_TECNICO_PASSWORD` | Senha do técnico `tecnico`; para demonstração, `tecnico123`. |

A conexão real com o banco deve permanecer nas variáveis do serviço, fora do repositório. Autorize também os endereços de saída do serviço no acesso de rede do Atlas.

Para testar o contêiner exclusivamente por HTTP local, defina `SERVER_SERVLET_SESSION_COOKIE_SECURE=false` na execução de teste. No perfil do Render, o cookie seguro é habilitado por padrão para o acesso HTTPS.

No plano gratuito, o serviço pode suspender após 15 minutos sem acessos; a próxima abertura pode levar cerca de um minuto. Os dados permanecem no Atlas. Consulte as [limitações do plano gratuito](https://render.com/docs/free).

As duas faixas de saída do serviço no Render foram autorizadas no Atlas. O fluxo de login, abertura e resolução foi validado pela URL pública, incluindo a consulta da solução pelo solicitante.
