[![CI](https://github.com/rogervinas/spring-boot-ai-langfuse-evaluations/actions/workflows/ci.yml/badge.svg)](https://github.com/rogervinas/spring-boot-ai-langfuse-evaluations/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-21-blue?labelColor=black)
![Kotlin](https://img.shields.io/badge/Kotlin-2.x-blue?labelColor=black)
![SpringBoot](https://img.shields.io/badge/SpringBoot-4.x-blue?labelColor=black)
![SpringAI](https://img.shields.io/badge/SpringAI-2.x-blue?labelColor=black)
![Langfuse](https://img.shields.io/badge/Langfuse-3.x-blue?labelColor=black)

![Gemini](https://img.shields.io/badge/Gemini-✓-4285F4?labelColor=black)
![Bedrock](https://img.shields.io/badge/Bedrock-✓-FF9900?labelColor=black)
![Ollama](https://img.shields.io/badge/Ollama-✓-FFFFFF?labelColor=black)

# Spring AI Langfuse Evaluations

> **A multi-model RAG and Tool-Calling assistant for modern fintech, built with Spring AI and evaluated with Langfuse for observability, RAG faithfulness, and tool-call accuracy.**

## Stack

- **Spring Boot** + **Spring AI**
- **LLM Providers**: AWS Bedrock, Google Gemini, or local Ollama
- **PGVector** as the vector database for RAG
- **Langfuse** for tracing and evaluation via OpenTelemetry

## Configuration

You can configure the application using environment variables or a `system.properties` file in the root directory. This file is ignored by Git and is loaded by both `./gradlew bootRun` and tests.

Example `system.properties`:

```properties
# AWS Bedrock
AWS_ACCESS_KEY_ID=...
AWS_SECRET_ACCESS_KEY=...
AWS_REGION=eu-central-1
AWS_BEDROCK_CHAT_MODEL=...
AWS_BEDROCK_EMBEDDING_MODEL=...

# Google Gemini
GOOGLE_API_KEY=...
```

## Running the local vector database

Start PGVector for the RAG vector database:

```bash
docker compose -f docker-compose-vectordb.yml up -d
```

This starts a PostgreSQL instance with the `pgvector` extension on port `5432`.

To stop it:

```bash
docker compose -f docker-compose-vectordb.yml down
```

To stop it and remove all volumes (removes all vector database data):

```bash
docker compose -f docker-compose-vectordb.yml down -v
```

## Running Ollama locally

If you want to use local LLMs, you can run Ollama either via Docker Compose or as a native application (more info at [ollama.com](https://ollama.com/)).

### Using Docker Compose

```bash
docker compose -f docker-compose-ollama.yml up -d
```

To stop it:

```bash
docker compose -f docker-compose-ollama.yml down
```


## Running Langfuse locally

The `docker-compose-langfuse.yml` is based on the [official Langfuse docker-compose.yml](https://github.com/langfuse/langfuse/blob/main/docker-compose.yml) with two modifications:
- All ports except `3000` (the main UI/API) are commented out to avoid collisions with other local containers
- `LANGFUSE_INIT_*` environment variables are set to auto-provision an organization, project, and user on first startup

Start the Langfuse stack (includes PostgreSQL, ClickHouse, Redis, MinIO):

```bash
docker compose -f docker-compose-langfuse.yml up -d
```

This auto-provisions:
- **Organization**: `rogervinas-bank`
- **Project**: `banking-sentinel`
- **API keys**: `publickey-local` / `secretkey-local`
- **User**: `admin@local.dev` / `password`

Langfuse UI is available at http://localhost:3000.

To stop it:

```bash
docker compose -f docker-compose-langfuse.yml down
```

To start from scratch (removes all data including traces, users, and projects):

```bash
docker compose -f docker-compose-langfuse.yml down -v
```

## Running the application

The `application.yml` is pre-configured to send traces to the local Langfuse instance using the auto-provisioned API keys.

You can run the application using one of the following profiles:

### 1. Ollama Profile (Local)

Requires [Ollama](#running-ollama-locally) and [Vector database](#running-the-local-vector-database) to be running.

```bash
SPRING_PROFILES_ACTIVE=ollama ./gradlew bootRun
```

### 2. AWS Bedrock Profile

Requires [Vector database](#running-the-local-vector-database) to be running. You need to configure AWS credentials and models via environment variables or `system.properties`:

```bash
# Set environment variables or use system.properties
SPRING_PROFILES_ACTIVE=bedrock ./gradlew bootRun
```

### 3. Google Gemini Profile

Requires [Vector database](#running-the-local-vector-database) to be running. You need to configure your Google AI API key via environment variables or `system.properties`:

```bash
# Set environment variables or use system.properties
SPRING_PROFILES_ACTIVE=gemini ./gradlew bootRun
```


## Spring AI + Spring Boot 4.x observability workarounds

Spring AI 2.0.0-M2 observation handlers (`ChatModelPromptContentObservationHandler`, `ChatModelCompletionObservationHandler`, etc.) only log to SLF4J instead of adding key values to the observation context, which means the data doesn't reach OTel-based backends like Langfuse.

Additionally, some auto-configurations depend on a Micrometer `Tracer` bean that Spring Boot 4.x no longer provides (it was part of `spring-boot-actuator-autoconfigure` in Spring Boot 3.x).

This project provides custom `ObservationFilter` implementations in `com.rogervinas.bank.observation` that add the data as high-cardinality key values using [OTel semantic conventions for GenAI](https://opentelemetry.io/docs/specs/semconv/gen-ai/gen-ai-metrics/):

| Filter | Replaces | OTel attributes |
|---|---|---|
| `ChatModelObservationFilter` | `ChatModelPromptContentObservationHandler` + `ChatModelCompletionObservationHandler` | `gen_ai.input.messages`, `gen_ai.output.messages`, `gen_ai.tool.definitions`, `gen_ai.response.model` (fix) |
| `ChatClientObservationFilter` | `ChatClientPromptContentObservationHandler` + `ChatClientCompletionObservationHandler` | `gen_ai.input.messages`, `gen_ai.output.messages` |
| `ImageModelObservationFilter` | `ImageModelPromptContentObservationHandler` | `gen_ai.input.messages` |
| `VectorStoreObservationFilter` | `VectorStoreQueryResponseObservationHandler` | `gen_ai.retrieval.documents` |
| `ToolCallingObservationFilter` | `ToolCallingContentObservationFilter` | `gen_ai.tool.name`, `gen_ai.tool.description`, `gen_ai.tool.call.arguments`, `gen_ai.tool.call.result` |

## Langfuse

## Requirements

[Langfuse CLI](https://langfuse.com/docs/api-and-data-platform/features/cli)
https://github.com/langfuse/langfuse-cli?tab=readme-ov-file#install

## Start Tracing ✅

## Prompt Management

https://langfuse.com/docs/prompt-management/overview

Via OTEL:
```
langfuse.observation.prompt.name: The name of your managed prompt
langfuse.observation.prompt.version: The version of the prompt
```

## Structured Input and Output

We use structured output (`ChatResponse` with `answer` + `suggestedActions` enum) instead of raw text for several reasons:

- **Deterministic evaluation of actions**: with an enum like `SuggestedAction.FREEZE_CARD` we can assert exact values in tests, no need for an LLM-as-judge to verify if "the agent offered to freeze the card"
- **LLM-as-judge only where it adds value**: the free-text `answer` still needs subjective evaluation (tone, completeness, correctness of explanation), so we keep LLM-as-judge for that part only
- **Better Langfuse traces**: structured JSON in trace output is filterable and aggregatable in dashboards, instead of opaque text blobs
- **Langfuse evaluators can target specific fields**: e.g. score `suggestedActions` correctness separately from `answer` quality
- **Datasets and experiments**: when creating Langfuse datasets, structured expected output (e.g. `expectedActions: [FREEZE_CARD, OPEN_DISPUTE]`) makes it trivial to compare against actual output
- **Frontend can render structured data**: action buttons, transaction tables, etc. instead of parsing markdown

We use Spring AI's `.entity(ChatResponse::class.java)` which leverages `BeanOutputConverter` to instruct the LLM to respond in the expected JSON schema and automatically deserialize it.

## Evals

### [Create a dataset](https://langfuse.com/docs/evaluation/experiments/datasets) to measure your LLM application's performance consistently

```bash
export LANGFUSE_PUBLIC_KEY=publickey-local
export LANGFUSE_SECRET_KEY=secretkey-local
export LANGFUSE_HOST=http://localhost:3000
```

Synthetic or from Production

### [Run an experiment](https://langfuse.com/docs/evaluation/core-concepts#experiments) get an overview of how your application is doing

### [Set up a live evaluator](https://langfuse.com/docs/evaluation/evaluation-methods/llm-as-a-judge) to monitor your live traces

### User feedback

https://langfuse.com/docs/evaluation/evaluation-methods/scores-via-sdk

```bash
curl -X POST http://localhost:3000/api/public/scores \
  -u "publickey-local:secretkey-local" \
  -H "Content-Type: application/json" \
  -d '{
    "traceId": "91d4fa4042cc6f3a3fc3b47c2a846331",
    "name": "correctness",
    "value": 0.9,
    "dataType": "NUMERIC",
    "comment": "Factually correct"
  }'
```
