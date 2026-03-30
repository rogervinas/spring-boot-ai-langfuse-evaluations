# TODO



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
