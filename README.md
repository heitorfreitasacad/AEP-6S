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

- Cadastrar uma nova doação (doador, item, quantidade, data);
- Consultar as doações registradas;
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
| **MongoDB** | Banco de dados NoSQL — guarda as doações |
| **Javalin** | Framework que expõe a API REST (endpoints HTTP) |
| **Jackson** | Converte objetos Java em JSON e vice-versa |
| **Swagger UI / OpenAPI** | Documentação interativa da API, testável no navegador |
| **JUnit 5 + Mockito** | Testes automatizados |
| **JaCoCo** | Mede a cobertura dos testes (mínimo 70% exigido) |

## Como rodar

### Pré-requisitos

- Java 17+
- Maven
- MongoDB rodando local (`brew services start mongodb-community no meu caso (MacOS)`, ou instância própria)

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
curl -X POST http://localhost:8080/doacoes -H "Content-Type: application/json" \
  -d '{"doador":"Maria","item":"Arroz","quantidade":10,"unidade":"kg","dataDoacao":"2026-08-20"}'

curl http://localhost:8080/doacoes
```

### Rodar a CLI (alternativa à API)

```bash
mvn exec:java
```

### Rodar os testes e ver o relatório

```bash
mvn test surefire-report:report
```

- Resultado dos testes (passou/falhou, tempo): `target/site/surefire-report.html`
- Cobertura de código (JaCoCo, mínimo 70%): `target/site/jacoco/index.html`

```bash
open target/site/surefire-report.html
open target/site/jacoco/index.html
```
