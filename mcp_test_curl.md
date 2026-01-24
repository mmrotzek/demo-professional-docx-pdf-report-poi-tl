## MCP HTTP endpoint testing with `curl` + `jq`

This project exposes an MCP server over **Streamable HTTP** at:

- **URL**: `http://localhost:8080/mcp/message`
- **Method**: `POST`
- **Protocol**: JSON‑RPC 2.0 over **Server‑Sent Events** (`text/event-stream`)

These examples assume:

- The Spring Boot app is running on `localhost:8080`
- `jq` is installed (e.g. `jq --version`)

---

## 1. Initialize a session and capture `Mcp-Session-Id`

The MCP HTTP transport requires a session. First, send an `initialize` request and grab the `Mcp-Session-Id` response header:

```bash
SESSION_HEADERS=$(curl -s -D - -o /dev/null \
  http://localhost:8080/mcp/message \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream, application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 0,
    "method": "initialize",
    "params": {
      "protocolVersion": "2025-03-26",
      "capabilities": {},
      "clientInfo": { "name": "curl-client", "version": "1.0.0" }
    }
  }')

SESSION_ID=$(printf '%s\n' "$SESSION_HEADERS" \
  | awk '/^Mcp-Session-Id:/ {print $2}' \
  | tr -d '\r')

echo "Session ID: $SESSION_ID"
```

Run this in the shell where you’ll issue subsequent commands so `$SESSION_ID` is available.

---

## 2. List available MCP tools (`tools/list`)

The server responds with Server‑Sent Events:

```text
id:<session-uuid>
event:message
data:{"jsonrpc":"2.0", ...}
```

Use `sed` to strip the `data:` prefix (with optional whitespace), then pipe into `jq`:

```bash
curl -s -N http://localhost:8080/mcp/message \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream, application/json" \
  -H "Mcp-Session-Id: $SESSION_ID" \
  -d '{
    "jsonrpc": "2.0",
    "id": 1,
    "method": "tools/list",
    "params": {}
  }' \
  | sed -n 's/^data:[[:space:]]*//p' \
  | jq .
```

To just see tool names:

```bash
curl -s -N http://localhost:8080/mcp/message \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream, application/json" \
  -H "Mcp-Session-Id: $SESSION_ID" \
  -d '{
    "jsonrpc": "2.0",
    "id": 1,
    "method": "tools/list",
    "params": {}
  }' \
  | sed -n 's/^data:[[:space:]]*//p' \
  | jq -r '.result.tools[].name'
```

---

## 3. Call a tool (`tools/call list_templates`)

Example for calling the `list_templates` tool:

```bash
curl -s -N http://localhost:8080/mcp/message \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream, application/json" \
  -H "Mcp-Session-Id: $SESSION_ID" \
  -d '{
    "jsonrpc": "2.0",
    "id": 2,
    "method": "tools/call",
    "params": {
      "name": "list_templates",
      "arguments": {}
    }
  }' \
  | sed -n 's/^data:[[:space:]]*//p' \
  | jq '.result'
```

The shape of the `list_templates` response is:

```json
{
  "jsonrpc": "2.0",
  "id": 2,
  "result": {
    "content": [
      {
        "type": "text",
        "text": "{\"success\":true,\"templates\":[],\"count\":0,\"error\":null,\"message\":null}"
      }
    ],
    "isError": false
  }
}
```

To pretty‑print the inner payload in `text`:

```bash
curl -s -N http://localhost:8080/mcp/message \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream, application/json" \
  -H "Mcp-Session-Id: $SESSION_ID" \
  -d '{
    "jsonrpc": "2.0",
    "id": 2,
    "method": "tools/call",
    "params": {
      "name": "list_templates",
      "arguments": {}
    }
  }' \
  | sed -n 's/^data:[[:space:]]*//p' \
  | jq -r '.result.content[0].text' \
  | jq .
```

---

## Notes

- The **Accept** header must include both `text/event-stream` and `application/json`. If you send only `application/json`, the server responds with `400 Invalid Accept headers. Expected TEXT_EVENT_STREAM and APPLICATION_JSON`.
- Always send the `Mcp-Session-Id` header after initialization; otherwise you’ll get `400 Session ID missing`.

