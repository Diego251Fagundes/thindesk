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
- MongoDB Community Server local (o comando `mongod` deve estar no `PATH`; neste computador, o executável já foi preparado na pasta `.local`).
- Postman, Insomnia ou ferramenta equivalente para utilizar a API.

## Como rodar localmente

1. Na raiz do projeto, inicie o MongoDB local:

   ```powershell
   powershell -ExecutionPolicy Bypass -File .\iniciar-mongodb.ps1
   ```

   O banco utiliza `mongodb://localhost:27017/thindesk`. Quando iniciado pelo script, seus dados ficam em `.local/mongodb/dados`, fora do Git e da pasta de compilação. Execute o script novamente após reiniciar o computador.
2. Na raiz do projeto, configure as variáveis de ambiente no PowerShell. Substitua as senhas de exemplo pelas suas senhas locais:

   ```powershell
   $env:THINDESK_PASSWORD = "sua-senha-de-usuario"
   $env:THINDESK_TECNICO_PASSWORD = "sua-senha-de-tecnico"
   ```

   As senhas devem ser definidas antes de iniciar a aplicação. A conexão está configurada para o MongoDB local em `application.properties`.

3. Compile e inicie a aplicação no mesmo terminal:

   ```powershell
   mvn package spring-boot:repackage
   java -jar target/thindesk-1.0-alpha.war
   ```

4. A aplicação estará disponível em `http://localhost:8080`, com login em `http://localhost:8080/login`.

## Como rodar pelo IntelliJ

1. Abra a pasta do projeto e importe o `pom.xml` como projeto Maven.
2. Em **File → Project Structure → Project SDK**, selecione o JDK 23.
3. Inicie o MongoDB pelo script acima.
4. Em **Run → Edit Configurations**, crie uma configuração do tipo **Application**, com a classe principal `com.pfc.ThindeskApplication` e o módulo `thindesk`.
5. Configure o JRE 23 e as variáveis `THINDESK_PASSWORD` e `THINDESK_TECNICO_PASSWORD` com suas senhas locais. Depois clique em **Run**.

Neste computador, a configuração **ThinDesk Local** já foi preparada com o Java 23 e as duas senhas de demonstração: `senha-local-usuario` para `admin` e `senha-local-tecnico` para `tecnico`. A configuração fica na pasta `.idea`, ignorada pelo Git. Após importar o Maven, selecione **ThinDesk Local** e clique em **Run**.

Para visualizar os dados no MongoDB Compass, conecte a `mongodb://localhost:27017`, abra o banco `thindesk` e a coleção `chamados`.

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

Os chamados ficam na coleção `chamados` do banco `thindesk`. Os dados podem ser consultados pelo MongoDB Compass para demonstrar a persistência.
