import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, beforeEach, vi } from "vitest";
import { EventRegistrationButton } from "../EventRegistrationButton";

vi.mock("@/contexts/AuthContext", () => ({
  useAuth: vi.fn(),
}));

vi.mock("@/app/actions/registrationActions", () => ({
  registerForEventAction: vi.fn(),
  cancelRegistrationAction: vi.fn(),
}));

import { useAuth } from "@/contexts/AuthContext";
import {
  registerForEventAction,
  cancelRegistrationAction,
} from "@/app/actions/registrationActions";

describe("EventRegistrationButton", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.stubGlobal("alert", vi.fn());
    vi.stubGlobal("confirm", vi.fn(() => true));
  });

  it("deve permitir entrar na lista de espera quando o evento estiver lotado", async () => {
    vi.mocked(useAuth).mockReturnValue({
      isAuthenticated: true,
      isLoading: false,
      login: vi.fn(),
      logout: vi.fn(),
      user: null,
    });
    vi.mocked(registerForEventAction).mockResolvedValue({
      status: "WAITING_LIST",
      message: "Evento lotado. Você foi adicionado(a) à lista de espera.",
    });

    render(
      <EventRegistrationButton
        eventId="event-1"
        isRegistered={false}
        userRegistrationStatus=""
        organizerEmail="org@test.com"
        isCanceled={false}
        isPast={false}
        isFull={true}
        isCompleted={false}
        organizers={[]}
      />,
    );

    const button = screen.getByRole("button", {
      name: /entrar na lista de espera/i,
    });
    await userEvent.click(button);

    await waitFor(() => {
      expect(registerForEventAction).toHaveBeenCalledWith("event-1");
      expect(globalThis.alert).toHaveBeenCalledWith(
        "Evento lotado. Você foi adicionado(a) à lista de espera.",
      );
    });
  });

  it("deve exibir opcao de sair da lista de espera quando usuario ja estiver aguardando", () => {
    vi.mocked(useAuth).mockReturnValue({
      isAuthenticated: true,
      isLoading: false,
      login: vi.fn(),
      logout: vi.fn(),
      user: null,
    });

    render(
      <EventRegistrationButton
        eventId="event-1"
        isRegistered={true}
        userRegistrationStatus="WAITING_LIST"
        organizerEmail="org@test.com"
        isCanceled={false}
        isPast={false}
        isFull={true}
        isCompleted={false}
        organizers={[]}
      />,
    );

    expect(
      screen.getByRole("button", { name: /sair da lista de espera/i }),
    ).toBeInTheDocument();
  });

  it("deve exibir links de gestao para o organizador do evento", () => {
    vi.mocked(useAuth).mockReturnValue({
      isAuthenticated: true,
      isLoading: false,
      login: vi.fn(),
      logout: vi.fn(),
      user: null,
    });

    render(
      <EventRegistrationButton
        eventId="event-1"
        isRegistered={false}
        userRegistrationStatus=""
        organizerEmail="org@test.com"
        isCanceled={false}
        isPast={false}
        isFull={false}
        isCompleted={false}
        organizers={[
          {
            id: "org-1",
            name: "Org Teste",
            contactEmail: "org@test.com",
          },
        ]}
      />,
    );

    expect(
      screen.getByRole("link", { name: /gerenciar presenças/i }),
    ).toHaveAttribute("href", "/events/event-1/presence");
    expect(
      screen.getByRole("link", { name: /ver lista de espera/i }),
    ).toHaveAttribute("href", "/events/event-1/waiting-list");
  });

  it("deve cancelar a fila de espera quando usuario sair da lista", async () => {
    vi.mocked(useAuth).mockReturnValue({
      isAuthenticated: true,
      isLoading: false,
      login: vi.fn(),
      logout: vi.fn(),
      user: null,
    });
    vi.mocked(cancelRegistrationAction).mockResolvedValue(undefined);

    render(
      <EventRegistrationButton
        eventId="event-1"
        isRegistered={true}
        userRegistrationStatus="WAITING_LIST"
        organizerEmail="org@test.com"
        isCanceled={false}
        isPast={false}
        isFull={true}
        isCompleted={false}
        organizers={[]}
      />,
    );

    await userEvent.click(
      screen.getByRole("button", { name: /sair da lista de espera/i }),
    );

    await waitFor(() => {
      expect(globalThis.confirm).toHaveBeenCalledWith(
        "Deseja sair da lista de espera deste evento?",
      );
      expect(cancelRegistrationAction).toHaveBeenCalledWith("event-1");
      expect(globalThis.alert).toHaveBeenCalledWith(
        "Você saiu da lista de espera com sucesso.",
      );
    });
  });

  it("nao deve cancelar a inscricao quando o usuario desistir na confirmacao", async () => {
    vi.mocked(useAuth).mockReturnValue({
      isAuthenticated: true,
      isLoading: false,
      login: vi.fn(),
      logout: vi.fn(),
      user: null,
    });
    vi.mocked(globalThis.confirm).mockReturnValue(false);

    render(
      <EventRegistrationButton
        eventId="event-1"
        isRegistered={true}
        userRegistrationStatus="CONFIRMED"
        organizerEmail="org@test.com"
        isCanceled={false}
        isPast={false}
        isFull={false}
        isCompleted={false}
        organizers={[]}
      />,
    );

    await userEvent.click(
      screen.getByRole("button", { name: /cancelar inscrição/i }),
    );

    await waitFor(() => {
      expect(globalThis.confirm).toHaveBeenCalledWith(
        "Deseja cancelar sua inscrição neste evento?",
      );
    });

    expect(cancelRegistrationAction).not.toHaveBeenCalled();
    expect(globalThis.alert).not.toHaveBeenCalled();
  });
});
