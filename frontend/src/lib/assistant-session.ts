const SESSION_KEY = "assistant-session-id";
const CONVERSATION_KEY = "assistant-conversation-id";

export function getAssistantSessionId(): string {
  if (typeof window === "undefined") {
    return "";
  }

  const existing = localStorage.getItem(SESSION_KEY);
  if (existing) {
    return existing;
  }

  const created = crypto.randomUUID();
  localStorage.setItem(SESSION_KEY, created);
  return created;
}

export function getStoredConversationId(): number | null {
  if (typeof window === "undefined") {
    return null;
  }

  const raw = localStorage.getItem(CONVERSATION_KEY);
  if (!raw) {
    return null;
  }

  const value = Number(raw);
  return Number.isFinite(value) ? value : null;
}

export function setStoredConversationId(conversationId: number | null) {
  if (typeof window === "undefined") {
    return;
  }

  if (conversationId === null) {
    localStorage.removeItem(CONVERSATION_KEY);
    return;
  }

  localStorage.setItem(CONVERSATION_KEY, String(conversationId));
}

export function startNewAssistantConversation() {
  setStoredConversationId(null);
}
