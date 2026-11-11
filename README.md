# AEP — Sistema de Gestão de Doações de Alimentos

**Engenharia de Software · UniCesumar · 2026.2 · 6º Semestre**

## O problema

Bancos de alimentos e pequenas ONGs que recebem doações de pessoas físicas ainda controlam tudo
em planilhas ou cadernos. Isso gera perda de informação (quantidade, validade, status), dificuldade
de saber o que já foi distribuído e falta de rastreabilidade entre o que entra e o que sai.

## Quem usa

ONGs e bancos de alimentos de pequeno porte, que recebem doações de itens não perecíveis de
doadores individuais e precisam acompanhar o ciclo da doação — do recebimento até a distribuição.

## Como o sistema ajuda

Centraliza o registro das doações em um único lugar, permitindo:

- Cadastrar as **instituições** (ONGs/bancos de alimentos) com endereço e contatos;
- Cadastrar uma nova doação (doador, item, quantidade, data) destinada a uma instituição;
- Consultar as doações registradas, inclusive as de uma instituição específica;
- Atualizar o status de uma doação (pendente → recebida → distribuída, ou cancelada);
- Remover um registro.

Com isso, a ONG passa a ter um histórico confiável do que recebeu e do que já distribuiu, em vez
de depender de planilha ou anotação manual.

## ODS atendido

**ODS 2 — Fome Zero e Agricultura Sustentável.** O sistema contribui para reduzir o desperdício de
doações e melhorar a eficiência da distribuição de alimentos para quem precisa.

## Tecnologias utilizadas

| Tecnologia | Pra que serve aqui |
|---|---|
| **Java 17** | Linguagem principal (orientada a objetos) |
| **Maven** | Compila o projeto e gerencia as dependências |
| **MongoDB** | Banco de dados NoSQL — duas coleções (`doacoes` e `instituicoes`) relacionadas por referência |
| **Javalin** | Framework que expõe a API REST (endpoints HTTP) |
| **Jackson** | Converte objetos Java em JSON e vice-versa |
| **Swagger UI / OpenAPI** | Documentação interativa da API, testável no navegador |
| **JUnit 5 + Mockito** | Testes automatizados |
| **JaCoCo** | Mede a cobertura dos testes (mínimo 70% exigido) |

## Como rodar

### Pré-requisitos

- Java 17 ou 21 (JDKs mais novos, como o 26, quebram os testes que usam Mockito)
- Maven
- MongoDB rodando em `localhost:27017` (ou outra instância, via variáveis `MONGODB_URI` e `MONGODB_DATABASE`)

### Subir o MongoDB

Escolha uma das opções:

```bash
# Homebrew (macOS): inicia o mongod direto, sem depender do brew services
mongod --config /opt/homebrew/etc/mongod.conf
```

```bash
# Docker
docker run -d --name aep-mongo -p 27017:27017 mongo:7
```

Para conferir que o banco está de pé:

```bash
mongosh --eval 'db.runCommand({ping:1})'
```

### Compilar

```bash
mvn compile
```

### Rodar a API REST

O `exec-maven-plugin` no `pom.xml` está fixo na CLI (`App`), então pra rodar a API é
preciso montar o classpath manualmente:

```bash
mvn dependency:build-classpath -Dmdep.outputFile=/tmp/cp.txt
java -cp "target/classes:$(cat /tmp/cp.txt)" br.com.unicesumar.aep.RestApp
```

A API sobe em `http://localhost:8080` (configurável via variável `PORT`).

### Acessar e testar a API

- **Swagger UI** (testa direto no navegador): http://localhost:8080/swagger-ui.html
- **Spec OpenAPI**: http://localhost:8080/openapi.yaml
- Ou via curl:

```bash
curl http://localhost:8080/instituicoes
curl http://localhost:8080/doacoes
```

Fluxo completo com o relacionamento (a doação exige uma instituição existente):

```bash
# 1) cadastrar a instituição e guardar o id retornado
curl -X POST http://localhost:8080/instituicoes -H "Content-Type: application/json" \
  -d '{"nome":"Banco de Alimentos de Maringa","endereco":{"rua":"Rua A","cidade":"Maringa","estado":"PR"},"contatos":[{"nome":"Ana","telefone":"44999990000","email":"ana@ong.org"}]}'

# 2) registrar a doação apontando para essa instituição
curl -X POST http://localhost:8080/doacoes -H "Content-Type: application/json" \
  -d '{"doador":"Maria","item":"Arroz","quantidade":10,"unidade":"kg","dataDoacao":"2026-08-20","instituicaoId":"<ID_DA_INSTITUICAO>"}'

# 3) listar as doações da instituição
curl http://localhost:8080/instituicoes/<ID_DA_INSTITUICAO>/doacoes
```

### Rodar a CLI (alternativa à API)

```bash
mvn exec:java
```

O menu cobre doações (1 a 4), instituições (5 a 8) e as doações de uma instituição (9).

### Rodar os testes e ver o relatório

```bash
mvn test surefire-report:report
```

Os testes **não precisam do MongoDB nem do Docker**: os repositórios, a API e a CLI são testados contra um
MongoDB em memória. Basta ter Java 17 ou 21 e Maven.

- Resultado dos testes (passou/falhou, tempo): `target/site/surefire-report.html`
- Cobertura de código (JaCoCo, mínimo 70%, o build falha abaixo disso): `target/site/jacoco/index.html`
- A cobertura é medida sobre todo o código, exceto a classe `MongoConfig` (conexão real com o banco).

```bash
open target/site/surefire-report.html
open target/site/jacoco/index.html
```

## Documentação técnica

- [Estrutura do banco de dados](docs/banco-de-dados.md): coleções, documentos de exemplo, relacionamento
- [Arquitetura](docs/arquitetura.md): camadas, pacotes, endpoints, erros, testes
- [Quadro de tarefas](docs/quadro-de-tarefas.md): metodologia de trabalho (Kanban)
- Especificação OpenAPI: `src/main/resources/public/openapi.yaml`

## Versões entregues

| Entrega | Tag | Conteúdo |
|---|---|---|
| 1ª | `entrega-1` | CRUD de doações, coleção única |
| 2ª | `entrega-2` | Coleção `instituicoes` (documento aninhado + lista de subdocumentos) e relacionamento com `doacoes` |
