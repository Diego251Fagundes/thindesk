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
- MongoDB em execução, localmente ou em um servidor acessível.
- Postman, Insomnia ou ferramenta equivalente para utilizar a API.

## Como rodar localmente

1. Inicie o MongoDB. Por padrão, a aplicação utiliza `mongodb://localhost:27017/thindesk` e o banco `thindesk`.
2. Na raiz do projeto, configure as variáveis de ambiente no PowerShell. Substitua as senhas de exemplo pelas suas senhas locais:

   ```powershell
   $env:THINDESK_PASSWORD = "sua-senha-de-usuario"
   $env:THINDESK_TECNICO_PASSWORD = "sua-senha-de-tecnico"
   $env:MONGODB_URI = "mongodb://localhost:27017/thindesk"
   ```

   A variável `MONGODB_URI` é opcional quando o MongoDB utiliza o endereço padrão. As senhas devem ser definidas antes de iniciar a aplicação. Não é necessário criar um arquivo `.env`.

3. Compile e inicie a aplicação no mesmo terminal:

   ```powershell
   mvn package spring-boot:repackage
   java -jar target/thindesk-1.0-alpha.war
   ```

4. A aplicação estará disponível em `http://localhost:8080`, com login em `http://localhost:8080/login`.

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
