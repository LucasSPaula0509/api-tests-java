# API Tests – DummyJSON (Java + Rest-Assured + JUnit 5 + Allure)

## Sumário
- [Sobre](#sobre)  
- [Arquitetura & Stack](#arquitetura--stack)  
- [Pré-requisitos](#pré-requisitos)  
- [Como baixar o projeto](#como-baixar-o-projeto)  
- [Como executar (IntelliJ)](#como-executar-intellij)  
- [Como executar (Maven/Terminal)](#como-executar-maventerminal)  
- [Relatório Allure](#relatório-allure)  
- [Estratégia & Plano de Testes](#estratégia--plano-de-testes)  
- [Cenários cobertos](#cenários-cobertos)  
- [Bugs encontrados / Divergências](#bugs-encontrados--divergências)  
- [Pipeline GitLab CI](#pipeline-gitlab-ci)  
- [Estrutura do projeto](#estrutura-do-projeto)  
- [Dicas & Troubleshooting](#dicas--troubleshooting)  
- [Nota sobre IA](#nota-sobre-ia)  

---

## Sobre
Testes funcionais da API de gerenciamento de produtos com base na documentação fornecida (DummyJSON).  
Cobrimos: disponibilidade, autenticação, produtos autenticados, listagem, criação, busca por id e fluxos de exceção.

---

## Arquitetura & Stack
- **Linguagem**: Java 17  
- **Build**: Maven  
- **Testes**: JUnit 5 + Rest-Assured  
- **Contrato/Schema**: JSON Schema Validator (Rest-Assured module)  
- **Relatórios**: Allure (plugin Maven)

---

## Pré-requisitos
- Java JDK 17+  
- Maven 3.9+  
- Git  
- (Opcional) Allure local para visualização via `allure:serve` (o plugin Maven já gera o relatório estático)

---

## Como baixar o projeto
```bash
git clone https://gitlab.com/<seu-usuario>/api-tests-java.git
cd api-tests-java
````

---

## Como executar (IntelliJ)

1. **Abrir o projeto** no IntelliJ (File → Open…).
2. Confirme **Project SDK** = 17 (File → Project Structure → Project/Modules).
3. Abra `pom.xml` e clique em **Reload Maven Project** (ícone do elefantinho).
4. Crie uma **Run Configuration** JUnit:

    * Run → Edit Configurations… → **+** → JUnit
    * Name: `All Tests`
    * Test kind: All in project
    * **VM options**:

      ```
      -DbaseUrl=https://dummyjson.com
      ```
    * Apply → OK
5. **Run** (Shift+F10) na config `All Tests`.

---

## Como executar (Maven/Terminal)

Na raiz do projeto:

```bash
# Executa todos os testes
mvn -q -DbaseUrl=https://dummyjson.com test
```

> Também é possível definir `BASE_URL` via variável de ambiente:
>
> * Windows PowerShell:
    >
    >   ```powershell
>   $env:BASE_URL="https://dummyjson.com"; mvn -q -DbaseUrl=$env:BASE_URL test
>   ```
> * Bash:
    >
    >   ```bash
>   export BASE_URL=https://dummyjson.com
>   mvn -q -DbaseUrl=$BASE_URL test
>   ```

---

## Relatório Allure

Gerar relatório estático + servir localmente:

```bash
# Após rodar os testes:
mvn allure:report
mvn allure:serve
```

* `allure:report` gera arquivos em `target/site/allure-maven-plugin/`
* `allure:serve` sobe um servidor local e abre o navegador automaticamente.

> Se preferir apenas o relatório estático (para anexar no CI), use só `mvn allure:report`.

---

## Estratégia & Plano de Testes

**Escopo:** disponibilidade, autenticação, endpoints protegidos, criação de produto, listagem e busca por id, incluindo **exceções**.

**Abordagem:** testes **funcionais** com Rest-Assured + **validação de contrato** (schemas mínimos).

**Dados:** usuários de `/users` (doc informa que `username` e `password` são relevantes) e payloads simples nos POSTs.

**Critérios de aceite (esperados por doc):**

* `/test` → 200 e `status=ok`
* `/auth/login` → cria token (doc sugere 201; **comportamento atual retorna 200**)
* `/auth/products`

    * com token válido → 200
    * sem token → doc sugere 403; comportamento atual retorna 401
    * token inválido → 401
* `POST /products/add` → 201 ecoando campos essenciais
* `GET /products` → 200, lista com `total > 0`, contrato mínimo
* `GET /products/{id}` → 200 quando existe, 404 quando não existe

---

## Cenários cobertos

* **Saúde**: `GET /test` (200, `status=ok`)
* **Auth**:

    * `GET /users` + `POST /auth/login` → token + contrato
* **Produtos protegidos**:

    * `GET /auth/products` com token válido → esperado 200
    * `GET /auth/products` sem token → esperado 403 (doc), real 401
    * `GET /auth/products` com token inválido → 401
* **Produtos públicos**:

    * `POST /products/add` → 201 (eco campos principais)
    * `GET /products` → 200 (contrato mínimo)
    * `GET /products/{id}` → 200 (existente), 404 (inexistente)

---

## Bugs encontrados / Divergências

### 1. `/auth/login` retorna **200** em vez de **201**

* **Esperado (documentação):** `POST /auth/login` → **201 Created**.
* **Obtido (execução real):** `200 OK`.
* **Impacto:** qualquer cliente que implemente com base na doc pode falhar (status code incorreto).
* **Correção sugerida ao desenvolvedor:**
  No controller/endpoint responsável pelo login, ajustar o retorno HTTP para `201 Created` em vez de `200`.

---

### 2. `/auth/products` rejeita token válido (retorna 401)

* **Fluxo seguido:**

    1. Buscar usuário em `/users`.
    2. Logar em `/auth/login` → token retornado.
    3. Usar token em `Authorization: Bearer <token>` para acessar `/auth/products`.
* **Esperado (documentação):** retorno `200` com lista de produtos.
* **Obtido (execução real):** `401 Unauthorized`.
* **Impacto:** login gera um token, mas esse token não é aceito pelo endpoint protegido → quebra de fluxo principal de negócio.
* **Correção sugerida ao desenvolvedor:**
  Validar se o token emitido pelo `/auth/login` está sendo corretamente persistido e aceito no middleware de autenticação de `/auth/products`. Pode haver:

    * divergência no algoritmo de assinatura do token,
    * expiração imediata.

---

### 3. `/auth/products` sem token retorna **401** em vez de **403**

* **Esperado (documentação):** ausência de credenciais → `403 Forbidden`.
* **Obtido (execução real):** `401 Unauthorized`.
* **Impacto:** documentação descreve código incorreto. Clientes podem tratar de forma errada a exceção.
* **Correção sugerida ao desenvolvedor:**
  Ajustar o código de status retornado quando não há token no header. O ideal:

    * **401 Unauthorized** = credenciais inválidas/expiradas.
    * **403 Forbidden** = ausência de credenciais.

---

## Pipeline GitLab CI

O projeto contém um `.gitlab-ci.yml` simples com dois estágios:

* `test` — roda `mvn test` e publica `target/allure-results` como artifact
* `report` — gera `mvn allure:report` e publica `target/site/allure-maven-plugin` como artifact

> Se quiser publicar via GitLab Pages, adicione um job `pages` movendo o relatório para `public/`.

---

## Estrutura do projeto

```
api-tests-java/
├─ pom.xml
├─ README.md
├─ .gitlab-ci.yml
└─ src
   └─ test
      ├─ java
      │  ├─ base/BaseApiTest.java
      │  ├─ util/{Env,UserClient,AuthClient,ProductClient}.java
      │  ├─ models/{LoginRequest,LoginResponse,ProductRequest}.java
      │  └─ tests/{HealthTest,AuthFlowTest,AuthProductsTest,ProductsCrudTest}.java
      └─ resources
         └─ schemas/{login-response.schema.json,products-list.schema.json}
```

---

## Dicas & Troubleshooting

* **Maven não resolve deps**: clique em **Reload Maven** no `pom.xml`.
* **Falha de schema**: confirme nomes/paths em `src/test/resources/schemas`.
* **Allure não abre**: rode `mvn allure:report` antes do `serve`. Feche servidores antigos.
* **Windows / Proxy**: se estiver atrás de proxy, configure `~/.m2/settings.xml`.

---

## Nota sobre IA

Este **README.md** foi desenvolvido com auxílio de **Inteligência Artificial (IA)**.
O QA responsável utiliza IA para aumentar a velocidade no desenvolvimento da automação de testes, mantendo foco em qualidade e detalhamento técnico.


