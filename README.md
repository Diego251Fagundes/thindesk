# ThinDesk

Projeto acadêmico em Java com Spring Boot, API REST e MongoDB para abertura, acompanhamento e resolução de chamados técnicos. A implementação reutiliza o login do projeto base do professor.

## Funcionalidades

- Abertura de chamados com título e descrição, registrando automaticamente solicitante, data de abertura e status `ABERTO`.
- Listagem e consulta dos chamados do usuário autenticado.
- Resolução por um técnico, registrando solução, técnico responsável, data de resolução e status `RESOLVIDO`.
- Validação dos campos obrigatórios e bloqueio de alterações em chamados resolvidos.
- Persistência dos dados no MongoDB.

## Pré-requisitos

- JDK 23 configurado no `JAVA_HOME` e no `PATH`.
- Maven instalado e disponível no terminal.
- Uma conta no MongoDB Atlas, com um cluster gratuito, usuário do banco e o IP do computador autorizado.
- Postman, Insomnia ou ferramenta equivalente para utilizar a API.

## Como rodar localmente

1. No Atlas, crie um usuário com permissão de leitura e escrita apenas no banco `thindesk`, autorize o IP do seu computador em **Network Access** e obtenha a conexão em **Connect → Drivers**.
2. Na raiz do projeto, configure as variáveis de ambiente no PowerShell. Substitua a conexão e as senhas de exemplo pelos seus valores:

   ```powershell
   $env:THINDESK_PASSWORD = "sua-senha-de-usuario"
   $env:THINDESK_TECNICO_PASSWORD = "sua-senha-de-tecnico"
   $env:THINDESK_MONGODB_URI = "mongodb+srv://USUARIO:SENHA@SEU_CLUSTER/thindesk?retryWrites=true&w=majority&authSource=admin"
   ```

   A conexão e as senhas devem ser definidas antes de iniciar a aplicação. A senha do banco é diferente das senhas de login do ThinDesk. Caso ela contenha caracteres especiais, utilize a versão codificada para URL na conexão. Mantenha os valores reais fora do Git.

   Se o endereço `mongodb+srv` apresentar erro de DNS, obtenha a conexão alternativa ativando **Legacy URI String** no Atlas. Use a conexão completa fornecida, com TLS/SSL habilitado.

3. Compile e inicie a aplicação no mesmo terminal:

   ```powershell
   mvn package spring-boot:repackage
   java -jar target/thindesk-1.0-alpha.war
   ```

4. A aplicação estará disponível em `http://localhost:8080`, com login em `http://localhost:8080/login`.

## Como rodar pelo IntelliJ

1. Abra a pasta do projeto e importe o `pom.xml` como projeto Maven.
2. Em **File → Project Structure → Project SDK**, selecione o JDK 23.
3. Confirme que o cluster do Atlas está disponível e que o IP do computador está autorizado.
4. Em **Run → Edit Configurations**, crie uma configuração do tipo **Application**, com a classe principal `com.pfc.ThindeskApplication` e o módulo `thindesk`.
5. Configure o JRE 23 e as variáveis `THINDESK_PASSWORD`, `THINDESK_TECNICO_PASSWORD` e `THINDESK_MONGODB_URI` com seus valores. Depois clique em **Run**.

As configurações locais do IntelliJ ficam em `.idea`, ignorada pelo Git. Para a demonstração, as senhas de login podem ser `senha-local-usuario` para `admin` e `senha-local-tecnico` para `tecnico`.

Neste computador, a configuração **ThinDesk Atlas** utiliza o arquivo privado `.local/atlas.properties` para carregar `THINDESK_MONGODB_URI`. Esse arquivo e as configurações da IDE não são enviados ao GitHub.

Para visualizar os dados no navegador, abra **Data Explorer** no Atlas, selecione o cluster e abra `thindesk → chamados`. O banco e as coleções são criados pela aplicação ao iniciar com uma conexão autorizada.

## Autenticação na API

No Postman ou Insomnia, envie `POST http://localhost:8080/login` com o corpo no formato `x-www-form-urlencoded`:

- `username`: `admin` para o solicitante ou `tecnico` para o técnico.
- `password`: a senha definida na variável de ambiente correspondente.

O login utiliza sessão. Mantenha o cookie `JSESSIONID` nas próximas requisições. Para trocar de usuário, envie `POST /logout` e faça login novamente.

## Endpoints

Todas as rotas abaixo exigem autenticação. Nos corpos JSON, utilize `Content-Type: application/json`.

| Método | Rota | Funcionalidade |
| --- | --- | --- |
| POST | `/api/chamados` | Abrir um chamado. |
| GET | `/api/chamados/usuario/{id}` | Listar os chamados do próprio usuário. |
| GET | `/api/chamados/{id}` | Consultar um chamado do próprio usuário. |
| PUT | `/api/chamados/{id}/resolver` | Resolver um chamado, com acesso exclusivo do técnico. |

Na rota de listagem por usuário, `{id}` corresponde ao nome de login, por exemplo `admin`. Nas demais rotas, corresponde ao identificador do chamado retornado pela API.

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

A abertura retorna `201`. Campos obrigatórios inválidos retornam `400`, acesso sem permissão retorna `403`, chamado inexistente retorna `404` e uma nova tentativa de resolver um chamado já resolvido retorna `409`.

Os chamados ficam na coleção `chamados` do banco `thindesk`. Atualize o Data Explorer do Atlas para demonstrar a persistência.

Se preferir usar MongoDB local, inicie o servidor com `powershell -ExecutionPolicy Bypass -File .\iniciar-mongodb.ps1` e defina `THINDESK_MONGODB_URI` como `mongodb://localhost:27017/thindesk`. Os dados do servidor iniciado pelo script ficam em `.local/mongodb/dados`, fora do Git.
