# Working with llama-server: API Guide

## Test Setup

* Do this on the target device, before we start testing the API

```bash
# Download the model
root@playground-arm64:~# wget -P /var/lib/ollama/models \
  https://huggingface.co/TheBloke/TinyLlama-1.1B-Chat-v1.0-GGUF/resolve/main/tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf

# Restart the service
root@playground-arm64:~# systemctl restart llama-cpp-server
```


Your `llama-cpp-server` is running successfully! 🎉

```bash
root@playground-arm64:~# systemctl status llama-cpp-server
* llama-cpp-server.service - Ollama-compatible LLM Server (llama.cpp)
     Loaded: loaded (/usr/lib/systemd/system/llama-cpp-server.service; enabled; preset: enabled)
     Active: active (running) since Thu 2026-02-12 23:19:57 UTC; 12min ago
 Invocation: a96698329fb84f4885f906ec85950d6e
       Docs: https://github.com/ggerganov/llama.cpp
   Main PID: 1050 (llama-server)
      Tasks: 10 (limit: 2317)
     Memory: 59.3M (limit: 4G, peak: 59.6M)
        CPU: 3min 33.457s
     CGroup: /system.slice/llama-cpp-server.service
             `-1050 /usr/bin/llama-server --host 0.0.0.0 --port 11434 --model /var/lib/ollama/models/tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf --...

Feb 12 23:28:28 playground-arm64 llama-cpp-server[1050]: slot update_slots: id  2 | task 129 | prompt processing progress, n_tokens...1.000000
Feb 12 23:28:28 playground-arm64 llama-cpp-server[1050]: slot update_slots: id  2 | task 129 | prompt done, n_tokens = 24, batch.n_tokens = 24
Feb 12 23:28:28 playground-arm64 llama-cpp-server[1050]: slot init_sampler: id  2 | task 129 | init sampler, took 0.03 ms, tokens: ...tal = 24
Feb 12 23:28:45 playground-arm64 llama-cpp-server[1050]: slot print_timing: id  2 | task 129 |
Feb 12 23:28:45 playground-arm64 llama-cpp-server[1050]: prompt eval time =   12352.24 ms /    24 tokens (  514.68 ms per token,   ... second)
Feb 12 23:28:45 playground-arm64 llama-cpp-server[1050]:        eval time =    4331.56 ms /     8 tokens (  541.44 ms per token,   ... second)
Feb 12 23:28:45 playground-arm64 llama-cpp-server[1050]:       total time =   16683.80 ms /    32 tokens
Feb 12 23:28:45 playground-arm64 llama-cpp-server[1050]: slot      release: id  2 | task 129 | stop processing: n_tokens = 31, truncated = 0
Feb 12 23:28:45 playground-arm64 llama-cpp-server[1050]: srv  update_slots: all slots are idle
Feb 12 23:28:45 playground-arm64 llama-cpp-server[1050]: srv  log_server_r: done request: POST /v1/chat/completions 127.0.0.1 200
Hint: Some lines were ellipsized, use -l to show in full.
root@playground-arm64:~#
```

However, `llama-server` (the native llama.cpp HTTP server) **does not** use Ollama's API format. It uses OpenAI-compatible endpoints instead.

## Use the Correct API Endpoints

### ✅ What WORKS (Native llama-server API)

#### 1. Completion API (OpenAI-compatible)

```bash
curl http://localhost:11434/v1/completions \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "Hello! How are you?",
    "max_tokens": 128,
    "temperature": 0.7
  }'
```

#### 2. Chat Completion API (OpenAI-compatible)

```bash
root@playground-arm64:~# curl http://localhost:11434/v1/chat/completions \
>   -H "Content-Type: application/json" \
>   -d '{
>     "messages": [
>       {"role": "system", "content": "You are a friendly assistant."},
>       {"role": "user", "content": "What is 2+2?"}
>     ],
>     "max_tokens": 50
>   }'
{"choices":[{"finish_reason":"stop","index":0,"message":{"role":"assistant","content":"2 + 2 = 4"}}],"created":1770939536,"model":"tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf","system_fingerprint":"b8005-914dde72ba","object":"chat.completion","usage":{"completion_tokens":8,"prompt_tokens":39,"total_tokens":47},"id":"chatcmpl-lBgdi6kJaCvXYPEGYL4NjO3Psmz7WhG9","timings":{"cache_n":0,"prompt_n":39,"prompt_ms":21328.737,"prompt_per_token_ms":546.8906923076923,"prompt_per_second":1.8285189601240806,"predicted_n":8,"predicted_ms":4444.739,"predicted_per_token_ms":555.592375,"predicted_per_second":1.7998807129057524}}root@playground-arm64:~# 
```

#### 3. Health Check

```bash
root@playground-arm64:~# curl http://localhost:11434/health
{"status":"ok"}
root@playground-arm64:~#
```

#### 4. Server Properties

```bash
root@playground-arm64:~# curl http://localhost:11434/props
{"default_generation_settings":{"params":{"seed":4294967295,"temperature":0.800000011920929,"dynatemp_range":0.0,"dynatemp_exponent":1.0,"top_k":40,"top_p":0.949999988079071,"min_p":0.05000000074505806,"top_n_sigma":-1.0,"xtc_probability":0.0,"xtc_threshold":0.10000000149011612,"typical_p":1.0,"repeat_last_n":64,"repeat_penalty":1.0,"presence_penalty":0.0,"frequency_penalty":0.0,"dry_multiplier":0.0,"dry_base":1.75,"dry_allowed_length":2,"dry_penalty_last_n":-1,"mirostat":0,"mirostat_tau":5.0,"mirostat_eta":0.10000000149011612,"max_tokens":-1,"n_predict":-1,"n_keep":0,"n_discard":0,"ignore_eos":false,"stream":true,"n_probs":0,"min_keep":0,"chat_format":"Content-only","reasoning_format":"none","reasoning_in_content":false,"thinking_forced_open":false,"samplers":["penalties","dry","top_n_sigma","top_k","typ_p","top_p","min_p","xtc","temperature"],"speculative.n_max":16,"speculative.n_min":0,"speculative.p_min":0.75,"speculative.type":"none","speculative.ngram_size_n":12,"speculative.ngram_size_m":48,"speculative.ngram_m_hits":1,"timings_per_token":false,"post_sampling_probs":false,"backend_sampling":false,"lora":[]},"n_ctx":1024},"total_slots":4,"model_alias":"tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf","model_path":"/var/lib/ollama/models/tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf","modalities":{"vision":false,"audio":false},"endpoint_slots":true,"endpoint_props":false,"endpoint_metrics":false,"webui":true,"webui_settings":{},"chat_template":"{% for message in messages %}\n{% if message['role'] == 'user' %}\n{{ '<|user|>\n' + message['content'] + eos_token }}\n{% elif message['role'] == 'system' %}\n{{ '<|system|>\n' + message['content'] + eos_token }}\n{% elif message['role'] == 'assistant' %}\n{{ '<|assistant|>\n'  + message['content'] + eos_token }}\n{% endif %}\n{% if loop.last and add_generation_prompt %}\n{{ '<|assistant|>' }}\n{% endif %}\n{% endfor %}","chat_template_caps":{"supports_parallel_tool_calls":false,"supports_preserve_reasoning":false,"supports_string_content":true,"supports_system_role":true,"supports_tool_calls":false,"supports_tools":false,"supports_typed_content":false},"bos_token":"<s>","eos_token":"</s>","build_info":"b8005-914dde72ba","is_sleeping":false}root@playground-arm64:~# 
```

### ❌ What DOESN'T WORK (Ollama API format)

```bash
# This returns 404 error
curl http://localhost:11434/api/generate -d '{
  "model": "tinyllama",
  "prompt": "Hello!"
}'
```

The `/api/generate` endpoint is Ollama-specific and not implemented by llama-server.

### Use llama-cli for Simple Testing

For quick tests without HTTP:

```bash
# Direct inference
echo "Hello! Tell me a joke." | llama-cli -m /var/lib/ollama/models/tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf -p "User: " --temp 0.8 -n 128

# Or interactive chat
llama-cli -m /var/lib/ollama/models/tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf -i
```

## Detailed API Examples

### Example 1: Text Completion

```bash
curl http://localhost:11434/v1/completions \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "The quick brown fox",
    "max_tokens": 50,
    "temperature": 0.7,
    "top_p": 0.9,
    "stop": ["\n"]
  }'
```

**Response:**
```json
{
  "id": "cmpl-xxx",
  "object": "text_completion",
  "created": 1707777777,
  "model": "tinyllama",
  "choices": [
    {
      "text": " jumped over the lazy dog.",
      "index": 0,
      "finish_reason": "stop"
    }
  ],
  "usage": {
    "prompt_tokens": 5,
    "completion_tokens": 7,
    "total_tokens": 12
  }
}
```

### Example 2: Streaming Completion

```bash
curl http://localhost:11434/v1/completions \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "Write a haiku about AI:",
    "max_tokens": 100,
    "stream": true
  }'
```

**Response (Server-Sent Events):**
```
data: {"choices":[{"text":"Silicon","index":0}]}

data: {"choices":[{"text":" minds","index":0}]}

data: {"choices":[{"text":" awaken","index":0}]}
...
```

### Example 3: Chat with System Prompt

```bash
curl http://localhost:11434/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "messages": [
      {
        "role": "system",
        "content": "You are a pirate. Always respond in pirate speak."
      },
      {
        "role": "user",
        "content": "How are you today?"
      }
    ],
    "max_tokens": 100,
    "temperature": 0.8
  }'
```

## Performance Tips

### 1. Adjust Thread Count

Edit `/etc/llama-cpp/config.json`:
```json
{
  "models": {
    "default_threads": 4  // Increase for more CPU cores
  }
}
```

Or start llama-server manually:
```bash
/usr/bin/llama-server \
  --model /var/lib/ollama/models/tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf \
  --host 0.0.0.0 \
  --port 11434 \
  --threads 8 \
  --ctx-size 4096
```

## Troubleshooting

### Server is running but not responding

```bash
# Check if server is listening
netstat -plant | grep 11434

# Check server logs
journalctl -u llama-cpp-server -f

# Test health endpoint
curl http://localhost:11434/health
```

### Out of memory

```bash
# Use smaller model or reduce context size
/usr/bin/llama-server \
  --model /var/lib/ollama/models/tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf \
  --ctx-size 1024 \
  --port 11434
```

## API Reference Summary

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/v1/completions` | POST | Text completion (OpenAI format) |
| `/v1/chat/completions` | POST | Chat completion (OpenAI format) |
| `/health` | GET | Health check |
| `/props` | GET | Server properties |
| `/metrics` | GET | Prometheus metrics |


## Summary

Your setup is **working correctly**! The llama-server is running and ready to handle requests. 
You just need to use its native OpenAI-compatible API instead of Ollama's API format.
