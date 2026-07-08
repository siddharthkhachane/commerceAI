"use client";

import Link from "next/link";
import { FormEvent, useEffect, useMemo, useRef, useState } from "react";

import { useShoppingContext } from "@/components/assistant/useShoppingContext";
import { sendAssistantChat } from "@/lib/assistant";
import {
  getAssistantSessionId,
  getStoredConversationId,
  setStoredConversationId,
  startNewAssistantConversation,
} from "@/lib/assistant-session";
import { formatPrice } from "@/lib/format";
import type { AssistantChatResponse, ChatMessage } from "@/types/assistant";

const starterPrompts = [
  "I'm going skiing. Budget $300.",
  "Find jackets under $150",
  "Recommend something for me based on my style",
];

type AssistantChatPanelProps = {
  viewingProductSlug?: string | null;
};

export function AssistantChatPanel({ viewingProductSlug = null }: AssistantChatPanelProps) {
  const { context, summary } = useShoppingContext({ viewingProductSlug });
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState("I'm going skiing. Budget $300.");
  const [budget, setBudget] = useState("300");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [lastMeta, setLastMeta] = useState<Pick<
    AssistantChatResponse,
    "usedConversationMemory" | "avoidedRepeatRecommendations" | "shoppingContext"
  > | null>(null);
  const bottomRef = useRef<HTMLDivElement | null>(null);

  const parsedBudget = useMemo(() => {
    const raw = budget.trim();
    if (!raw) return undefined;
    const value = Number(raw);
    return Number.isFinite(value) && value > 0 ? value : undefined;
  }, [budget]);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, loading]);

  async function sendMessage(messageText: string) {
    const trimmed = messageText.trim();
    if (!trimmed || loading) {
      return;
    }

    setLoading(true);
    setError(null);
    setMessages((current) => [
      ...current,
      { id: crypto.randomUUID(), role: "user", content: trimmed },
    ]);

    try {
      const response = await sendAssistantChat({
        conversationId: getStoredConversationId(),
        sessionId: getAssistantSessionId(),
        message: trimmed,
        budget: parsedBudget,
        context,
      });

      setStoredConversationId(response.conversationId);
      setLastMeta({
        usedConversationMemory: response.usedConversationMemory,
        avoidedRepeatRecommendations: response.avoidedRepeatRecommendations,
        shoppingContext: response.shoppingContext,
      });

      setMessages((current) => [
        ...current,
        {
          id: crypto.randomUUID(),
          role: "assistant",
          content: response.reply,
          toolCalls: response.toolCalls,
          recommendations: response.recommendations,
        },
      ]);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not reach the assistant.");
    } finally {
      setLoading(false);
    }
  }

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const current = input;
    setInput("");
    await sendMessage(current);
  }

  function handleNewChat() {
    startNewAssistantConversation();
    setMessages([]);
    setLastMeta(null);
    setError(null);
  }

  return (
    <section className="mx-auto max-w-7xl px-4 py-16 sm:px-6 md:py-20">
      <div className="overflow-hidden rounded-3xl border border-border bg-card shadow-sm">
        <div className="border-b border-border px-6 py-5 sm:px-8">
          <div className="flex flex-wrap items-start justify-between gap-4">
            <div>
              <p className="text-sm font-medium uppercase tracking-[0.2em] text-muted-foreground">
                AI Shopping Assistant
              </p>
              <h2 className="mt-2 text-3xl font-semibold tracking-tight text-foreground sm:text-4xl">
                Chat with memory, tools, and your shopping context
              </h2>
              <p className="mt-3 max-w-2xl text-sm text-muted-foreground sm:text-base">
                The assistant remembers this conversation, calls multiple tools, and uses your
                cart, favorites, and compare list when you are signed in.
              </p>
            </div>
            <button
              type="button"
              onClick={handleNewChat}
              className="rounded-full border border-border px-4 py-2 text-sm font-medium text-foreground transition hover:bg-card-hover"
            >
              New chat
            </button>
          </div>

          <div className="mt-4 flex flex-wrap gap-2">
            <ContextChip label={`Cart ${summary.cartCount}`} />
            <ContextChip label={`Saved ${summary.savedCount}`} />
            <ContextChip label={`Compare ${summary.compareCount}`} />
            {summary.viewingSlug ? <ContextChip label={`Viewing ${summary.viewingSlug}`} /> : null}
          </div>
        </div>

        <div className="max-h-[32rem] space-y-4 overflow-y-auto px-6 py-5 sm:px-8">
          {messages.length === 0 ? (
            <div className="rounded-2xl border border-dashed border-border bg-muted/30 px-5 py-8">
              <p className="text-sm text-muted-foreground">
                Try a starter prompt or ask a follow-up like &quot;show me more like those under
                $150&quot;.
              </p>
              <div className="mt-4 flex flex-wrap gap-2">
                {starterPrompts.map((prompt) => (
                  <button
                    key={prompt}
                    type="button"
                    onClick={() => void sendMessage(prompt)}
                    className="rounded-full border border-border bg-background px-3 py-1.5 text-xs font-medium text-foreground transition hover:bg-card-hover"
                  >
                    {prompt}
                  </button>
                ))}
              </div>
            </div>
          ) : null}

          {messages.map((message) => (
            <div
              key={message.id}
              className={`flex ${message.role === "user" ? "justify-end" : "justify-start"}`}
            >
              <div
                className={`max-w-[90%] rounded-2xl px-4 py-3 text-sm ${
                  message.role === "user"
                    ? "bg-foreground text-background"
                    : "border border-border bg-background text-foreground"
                }`}
              >
                <p className="whitespace-pre-wrap">{message.content}</p>

                {message.toolCalls && message.toolCalls.length > 0 ? (
                  <div className="mt-3 flex flex-wrap gap-2">
                    {message.toolCalls.map((tool) => (
                      <span
                        key={`${message.id}-${tool.toolName}`}
                        className="rounded-full bg-muted px-2.5 py-1 text-[11px] font-medium text-muted-foreground"
                      >
                        {formatToolName(tool.toolName)}
                      </span>
                    ))}
                  </div>
                ) : null}

                {message.recommendations && message.recommendations.length > 0 ? (
                  <div className="mt-4 grid gap-3 sm:grid-cols-2">
                    {message.recommendations.map((item) => (
                      <article
                        key={item.product.id}
                        className="rounded-xl border border-border bg-card p-3"
                      >
                        <p className="text-[11px] uppercase tracking-[0.14em] text-muted-foreground">
                          {item.product.category.name}
                        </p>
                        <h3 className="mt-1 text-sm font-semibold text-foreground">
                          {item.product.name}
                        </h3>
                        <p className="mt-1 text-sm font-medium">{formatPrice(item.product.price)}</p>
                        <p className="mt-2 text-xs text-muted-foreground">{item.reason}</p>
                        <Link
                          href={`/products/${item.product.slug}`}
                          className="mt-2 inline-flex text-xs font-medium underline-offset-4 hover:underline"
                        >
                          View product
                        </Link>
                      </article>
                    ))}
                  </div>
                ) : null}
              </div>
            </div>
          ))}

          {loading ? (
            <p className="text-sm text-muted-foreground">Assistant is thinking…</p>
          ) : null}
          <div ref={bottomRef} />
        </div>

        {lastMeta ? (
          <div className="border-t border-border px-6 py-3 text-xs text-muted-foreground sm:px-8">
            {lastMeta.usedConversationMemory ? "Used conversation memory. " : ""}
            {lastMeta.avoidedRepeatRecommendations > 0
              ? `Avoided ${lastMeta.avoidedRepeatRecommendations} repeat recommendations. `
              : ""}
            {lastMeta.shoppingContext.stylePreference
              ? `Style preference: ${lastMeta.shoppingContext.stylePreference}. `
              : ""}
          </div>
        ) : null}

        <form
          onSubmit={onSubmit}
          className="border-t border-border px-6 py-4 sm:px-8 sm:py-5"
        >
          {error ? (
            <p className="mb-3 rounded-xl border border-rose-300 bg-rose-50 px-4 py-3 text-sm text-rose-700 dark:border-rose-800 dark:bg-rose-950/50 dark:text-rose-300">
              {error}
            </p>
          ) : null}
          <div className="grid gap-3 sm:grid-cols-[1fr_150px_auto]">
            <input
              value={input}
              onChange={(event) => setInput(event.target.value)}
              placeholder="Ask anything about products, compare items, or get personalized picks"
              className="h-11 rounded-xl border border-input bg-background px-4 text-sm text-foreground outline-none transition focus:border-ring focus:ring-2 focus:ring-ring/30"
              required
            />
            <input
              value={budget}
              onChange={(event) => setBudget(event.target.value)}
              placeholder="Budget"
              inputMode="decimal"
              className="h-11 rounded-xl border border-input bg-background px-4 text-sm text-foreground outline-none transition focus:border-ring focus:ring-2 focus:ring-ring/30"
            />
            <button
              type="submit"
              disabled={loading}
              className="h-11 rounded-xl bg-foreground px-5 text-sm font-medium text-background transition hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-60"
            >
              Send
            </button>
          </div>
        </form>
      </div>
    </section>
  );
}

function ContextChip({ label }: { label: string }) {
  return (
    <span className="rounded-full border border-border bg-background px-3 py-1 text-xs font-medium text-muted-foreground">
      {label}
    </span>
  );
}

function formatToolName(toolName: string): string {
  return toolName.replaceAll("_", " ");
}
