# API Tests — DummyJSON (Java + Rest-Assured + JUnit + Allure)

[![Testes de API](https://github.com/LucasSPaula0509/api-tests-java/actions/workflows/tests.yml/badge.svg)](https://github.com/LucasSPaula0509/api-tests-java/actions/workflows/tests.yml)
[![Lint](https://github.com/LucasSPaula0509/api-tests-java/actions/workflows/lint.yml/badge.svg)](https://github.com/LucasSPaula0509/api-tests-java/actions/workflows/lint.yml)
[![Allure Report](https://img.shields.io/badge/Allure-Report-orange)](https://lucasspaula0509.github.io/api-tests-java/)

**Allure Report da última execução da `main`:** <https://lucasspaula0509.github.io/api-tests-java/>

---

## Sumário

1. [Sobre](#1-sobre)
2. [Stack](#2-stack)
3. [Pré-requisitos e instalação](#3-pré-requisitos-e-instalação)
4. [Como rodar](#4-como-rodar)
5. [Relatório Allure](#5-relatório-allure)
6. [Estrutura do projeto](#6-estrutura-do-projeto)
7. [Cenários cobertos](#7-cenários-cobertos)
8. [Tags](#8-tags)
9. [Defeitos conhecidos (divergências da documentação)](#9-defeitos-conhecidos-divergências-da-documentação)
10. [CI/CD](#10-cicd)
11. [Como criar um novo teste](#11-como-criar-um-novo-teste)
12. [Decisões técnicas](#12-decisões-técnicas)
13. [Troubleshooting](#13-troubleshooting)
14. [Nota sobre IA](#14-nota-sobre-ia)

---

## 1. Sobre

Testes funcionais e de contrato da API pública de demonstração **DummyJSON** (<https://dummyjson.com>): disponibilidade, autenticação, endpoints protegidos e produtos (listagem, paginação, busca, categoria, criação, atualização e exclusão), incluindo os fluxos de exceção.

Quando a API diverge da documentação, o teste continua esperando o comportamento documentado e fica marcado como **defeito conhecido** (seção 9): ele falha enquanto a divergência existir, fora da execução padrão.

---

## 2. Stack

| Componente | Ferramenta |
|---|---|
| Linguagem e build | Java 21 e Maven (Maven Wrapper incluído) |
| Testes | JUnit 6 + Rest-Assured 6 |
| Asserções | AssertJ (com descrição em cada asserção) |
| Contrato | JSON Schema (módulo `json-schema-validator` do Rest-Assured) |
| Relatório | Allure 2 (`allure-junit5` + `allure-rest-assured`) |
| Formatação | Spotless (google-java-format) |
| CI | GitHub Actions (principal) e GitLab CI |

---

## 3. Pré-requisitos e instalação

| Ferramenta | Para quê |
|---|---|
| **JDK 21** | compilar e rodar os testes ([Temurin 21](https://adoptium.net/)) |
| **Maven** | **não precisa instalar**: use o wrapper `./mvnw` (Linux/macOS) ou `mvnw.cmd` (Windows) |
| **Git** | clonar o repositório |

```bash
git clone https://github.com/LucasSPaula0509/api-tests-java.git
cd api-tests-java
./mvnw -v      # na primeira vez, baixa o Maven automaticamente
```

> **Tem outro JDK no `JAVA_HOME`?** Declare o JDK 21 em `~/.m2/toolchains.xml`; quando o arquivo existe, o build usa esse JDK automaticamente.

**IntelliJ:** abra a pasta (File → Open), confirme o SDK 21 em Project Structure e rode os testes pela própria IDE (clique no ícone ao lado da classe ou do método). Não é preciso configurar VM options: a URL padrão já é `https://dummyjson.com`.

---

## 4. Como rodar

> No Windows, troque `./mvnw` por `mvnw.cmd` (ou use o Git Bash).

```bash
./mvnw clean test                                          # tudo, exceto @defeito
./mvnw clean test -Dgroups=smoke                           # só o smoke
./mvnw clean test -Dgroups=defeito -DexcludedGroups=none   # defeitos conhecidos (devem falhar)
./mvnw clean test -Dtest=ProductsTest                      # uma classe
./mvnw clean test -DbaseUrl=https://outra-url              # outra URL da API
./mvnw spotless:check                                      # formatação (spotless:apply corrige)
```

A URL da API segue esta prioridade: `-DbaseUrl=...` → variável de ambiente `BASE_URL` → `https://dummyjson.com`.

---

## 5. Relatório Allure

```bash
./mvnw allure:serve     # gera e abre no navegador
./mvnw allure:report    # gera em target/site/allure-maven-plugin/
```

No relatório:

- cada chamada HTTP aparece como passo, com a **requisição e a resposta anexadas** (`AllureRestAssured`);
- os testes ficam agrupados por **Feature** (Saúde, Autenticação, Produtos autenticados, Produtos);
- os defeitos conhecidos ficam na categoria **"Defeitos conhecidos"**, com link do ID (`DEF-xxx`);
- o ambiente mostra a URL da API, a versão do Java e o sistema operacional.

---

## 6. Estrutura do projeto

```text
├── .github/workflows/
│   ├── tests.yml              # smoke → regressão → defeitos → Allure no GitHub Pages
│   └── lint.yml               # actionlint + Spotless
├── .gitlab-ci.yml             # pipeline equivalente no GitLab
├── pom.xml
└── src/test/
    ├── allure/categories.json # categorias do Allure (copiado para allure-results)
    ├── java/br/com/dummyjson/
    │   ├── config/Env.java    # URL da API (-DbaseUrl > BASE_URL > padrão)
    │   ├── client/            # Specs (spec base) e um client por recurso: Auth, Product, User
    │   ├── model/             # records de requisição e resposta
    │   ├── data/              # massa de dados (TestData) e tags (Tags)
    │   ├── allure/            # enriquecimento dos resultados e environment.properties
    │   └── tests/             # Health, Auth, AuthProducts, Products
    └── resources/
        ├── schemas/           # JSON Schemas do login e da listagem de produtos
        ├── allure.properties  # padrão do link de issue
        └── META-INF/services/ # registro dos listeners do Allure e do JUnit
```

Os **clients** montam as chamadas (com `@Step` do Allure) e devolvem a `Response`; os **testes** fazem as asserções. Não há estado global do Rest-Assured: toda chamada usa a spec de `Specs.json()`.

---

## 7. Cenários cobertos

| Funcionalidade | Cenário | Esperado |
|---|---|---|
| Saúde | `GET /test` | 200, `status=ok`, `method=GET` |
| Autenticação | login com usuário de `/users` | tokens (`accessToken`, `refreshToken`) e contrato do schema |
| | login com credenciais válidas | **201** (documentação) — DEF-001 |
| | login com senha incorreta | 400, `Invalid credentials` |
| | `GET /auth/me` com token | 200 e o usuário autenticado |
| | `POST /auth/refresh` | 200 e novos tokens |
| Produtos autenticados | `GET /auth/products` com token válido | 200 e lista não vazia |
| | sem o cabeçalho `Authorization` | **403** (documentação) — DEF-002 |
| | token inválido | 401, `Invalid/Expired Token` |
| | JWT malformado | **401** — DEF-003 |
| Produtos | `GET /products` | 200, `total > 0` e contrato do schema |
| | paginação (`limit`, `skip`, `select`) | a página e só os campos pedidos |
| | busca (`/products/search?q=`) | resultados encontrados |
| | filtro por categoria | só produtos da categoria |
| | `GET /products/{id}` existente / inexistente | 200 com campos essenciais / 404 `not found` |
| | `POST /products/add` | 201 e eco dos campos enviados |
| | `PUT /products/{id}` | 200 com o campo alterado |
| | `DELETE /products/{id}` | 200, `isDeleted=true` e `deletedOn` |

---

## 8. Tags

| Tag | Significado |
|---|---|
| `smoke` | caminho crítico; roda primeiro no CI |
| `regressivo` | faz parte da regressão |
| `defeito` | divergência conhecida da documentação; **fora** da execução padrão, com job próprio no CI |

O filtro padrão está no `pom.xml` (`excludedGroups=defeito`).

---

## 9. Defeitos conhecidos (divergências da documentação)

Os testes `@defeito` **continuam esperando o comportamento documentado**. Eles falham enquanto a divergência existir e rodam no job `known-defects`, que não quebra o pipeline.

| ID | Teste | Esperado (documentação) | Obtido | Impacto |
|---|---|---|---|---|
| DEF-001 | `AuthTest.loginReturnsCreated` | `POST /auth/login` → **201 Created** | 200 OK | clientes que seguem a documentação tratam o status errado |
| DEF-002 | `AuthProductsTest.missingTokenIsForbidden` | sem credenciais → **403 Forbidden** | 401 `Access Token is required` | o código de erro diverge da documentação |
| DEF-003 | `AuthProductsTest.malformedTokenIsUnauthorized` | JWT malformado → **401** | **500** `invalid token` | entrada inválida do cliente gera erro de servidor |

**Correções sugeridas:**

- **DEF-001:** retornar `201 Created` no login, ou corrigir a documentação para `200`.
- **DEF-002:** alinhar código e documentação (o mais comum é `401` para credencial ausente ou inválida e `403` para usuário autenticado sem permissão).
- **DEF-003:** tratar a falha de decodificação do token e responder `401 Unauthorized`.

> O antigo "bug 2" (token válido recusado no `/auth/products`) **não era da API**: o login passou a devolver `accessToken`, e o teste ainda lia o campo `token`, mandando um token nulo. Com o campo atual, o endpoint responde 200.

Quando uma divergência for resolvida, o teste passa no job `known-defects`. Remova então a tag `defeito` e mantenha o teste na regressão.

---

## 10. CI/CD

### GitHub Actions (`tests.yml`): a cada push/PR na `main`, todo dia às 09:00 (BRT) e sob demanda

```text
smoke ──► regressão ──► defeitos conhecidos ──► Allure Report ──► GitHub Pages (só na main)
```

| Job | O que faz |
|---|---|
| **smoke** | `-Dgroups=smoke`. Se falhar, a regressão nem começa. |
| **regression** | todos os testes, exceto `defeito` (ou as tags escolhidas na execução manual) |
| **known-defects** | só `defeito`, com `continue-on-error`. Deve ficar vermelho enquanto as divergências existirem. |
| **report** | junta os `allure-results`, recupera o histórico do relatório anterior, gera o Allure e publica em <https://lucasspaula0509.github.io/api-tests-java/>. Em PR, o relatório fica só como artifact. |

O `lint.yml` roda o `actionlint` nos workflows e o `spotless:check` no código.

### GitLab CI (`.gitlab-ci.yml`)

Mesmos estágios (smoke, regressão, defeitos com `allow_failure` e relatório Allure como artifact), com Java 21 e o Maven Wrapper.

---

## 11. Como criar um novo teste

1. **Client:** se o endpoint ainda não existe, adicione um método no client do recurso (`client/`), usando `given().spec(Specs.json())` e `@Step("MÉTODO /caminho")`.
2. **Massa de dados:** valores fixos vão em `data/TestData.java`, não no teste.
3. **Teste:** na classe da funcionalidade (`tests/`), crie o método com `@Test`, `@DisplayName` em português e as tags (`@Tag(Tags.REGRESSION)`, e `@Tag(Tags.SMOKE)` se for caminho crítico).
4. **Asserções** com AssertJ e descrição: `assertThat(response.statusCode()).as("Status de ...").isEqualTo(200)`.
5. **Contrato:** para validar a estrutura, crie o schema em `resources/schemas/` e use `matchesJsonSchemaInClasspath(...)`.
6. **Divergência da documentação?** Mantenha o esperado documentado, adicione `@Tag(Tags.KNOWN_DEFECT)` e `@Issue("DEF-xxx")` e registre o defeito na seção 9.
7. Rode `./mvnw clean test` e `./mvnw spotless:apply`.

---

## 12. Decisões técnicas

| Decisão | Motivo |
|---|---|
| Java 21, JUnit 6 e Rest-Assured 6 | versões estáveis mais recentes; o JUnit 6 exige Java 17+ |
| Maven Wrapper | ninguém precisa instalar o Maven; todos usam a mesma versão (3.9.16) |
| Spec base (`Specs.json()`) em vez de `RestAssured.baseURI` | sem estado global: cada chamada é independente e pode rodar em qualquer ordem |
| AssertJ no lugar do Hamcrest | mensagens de falha com a descrição do que estava sendo validado |
| `AllureRestAssured` + `@Step` nos clients | requisição e resposta de cada chamada ficam no relatório, sem log manual |
| Records para os modelos | menos código e objetos imutáveis |
| Divergências como `@Tag("defeito")` | o resultado esperado da documentação não é alterado para o teste passar; o defeito fica visível e separado da regressão |
| Login separado em dois testes | um valida tokens e contrato (comportamento), o outro só o status documentado (DEF-001); assim o defeito não esconde a validação do contrato |
| Schema do login com `accessToken` | é o campo que a API retorna hoje e o que a documentação do DummyJSON descreve |
| Credenciais lidas de `GET /users` | a API de demonstração expõe a senha; o teste não depende de um usuário fixo |
| GitHub Actions como CI principal, GitLab CI mantido | o repositório é público no GitHub; o pipeline do GitLab continua equivalente |
| `-DexcludedGroups=none` para rodar os defeitos | `none` não é uma tag usada, então nada é excluído e o `-Dgroups=defeito` vale sozinho |

---

## 13. Troubleshooting

| Problema | Causa / solução |
|---|---|
| **`release version 21 not supported`** | o Maven está rodando em um JDK anterior ao 21: ajuste o `JAVA_HOME` ou crie o `~/.m2/toolchains.xml` |
| **`./mvnw: Permission denied`** (Linux/macOS) | `chmod +x mvnw` |
| **`spotless:check` falhou** | rode `./mvnw spotless:apply` e faça o commit da formatação |
| **Falha de schema** | o contrato da API mudou ou o schema está desatualizado: compare a resposta anexada no Allure com o arquivo em `resources/schemas/` |
| **O job "Defeitos conhecidos" está vermelho** | esperado enquanto as divergências existirem; o workflow continua verde (`continue-on-error`) |
| **Atrás de proxy** | configure o proxy em `~/.m2/settings.xml` (Maven) e `-Dhttps.proxyHost`/`-Dhttps.proxyPort` (testes) |
| **Allure não abre** | use `./mvnw allure:serve`; para o HTML gerado, abra por um servidor, não direto do disco |

---

## 14. Nota sobre IA

Este projeto foi desenvolvido com auxílio de **Inteligência Artificial (IA)**. O QA responsável utiliza IA para aumentar a velocidade no desenvolvimento da automação de testes, mantendo foco em qualidade e detalhamento técnico.
