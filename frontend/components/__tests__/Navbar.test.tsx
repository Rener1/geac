import { render, screen, fireEvent } from "@testing-library/react";
import { Navbar } from "../Navbar";
import { describe, it, expect, vi, beforeEach } from "vitest";
import * as AuthContext from "@/contexts/AuthContext";

vi.mock("next/link", () => ({
  default: ({
    children,
    href,
  }: {
    children: React.ReactNode;
    href: string;
  }) => <a href={href}>{children}</a>,
}));

vi.mock("next/navigation", () => ({
  usePathname: vi.fn(() => "/"),
}));

const useAuthSpy = vi.spyOn(AuthContext, "useAuth");

describe("Navbar Component", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("deve mostrar botões de Login/Cadastro quando NÃO autenticado", () => {
    useAuthSpy.mockReturnValue({
      isAuthenticated: false,
      user: null,
      isLoading: false,
      login: vi.fn(),
      logout: vi.fn(),
    });

    render(<Navbar />);

    expect(screen.getByText("Entrar")).toBeInTheDocument();
    expect(screen.getByText("Cadastrar")).toBeInTheDocument();
    expect(screen.queryByText("Sair")).not.toBeInTheDocument();
  });

  it("deve mostrar Menu do Usuário e Logout quando autenticado", () => {
    useAuthSpy.mockReturnValue({
      isAuthenticated: true,
      user: {
        //@ts-expect-error - id é opcional, mas para o teste precisamos
        id: "1",
        name: "Dev Teste",
        email: "dev@test.com",
        role: "STUDENT",
      },
      isLoading: false,
      login: vi.fn(),
      logout: vi.fn(),
    });

    render(<Navbar />);

    expect(screen.getByText("Dev Teste")).toBeInTheDocument();
    expect(screen.getByText("Meus Eventos")).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: /sair da conta/i }),
    ).toBeInTheDocument();
  });

  it('deve chamar logout e mudar texto para "Saindo..." ao clicar em Sair', async () => {
    let resolveLogout: (value?: unknown) => void = () => {};
    const mockLogout = vi.fn().mockImplementation(() => {
      return new Promise((resolve) => {
        resolveLogout = resolve;
      });
    });

    useAuthSpy.mockReturnValue({
      isAuthenticated: true,
      //@ts-expect-error - id é opcional, mas para o teste precisamos
      user: { id: "1", name: "User", email: "u@u.com", role: "STUDENT" },
      isLoading: false,
      login: vi.fn(),
      logout: mockLogout,
    });

    render(<Navbar />);

    const logoutBtn = screen.getByRole("button", { name: /sair da conta/i });
    fireEvent.click(logoutBtn);

    expect(logoutBtn).toHaveTextContent("Saindo...");
    expect(mockLogout).toHaveBeenCalled();

    if (resolveLogout) {
      resolveLogout();
    }
  });

  it('deve resetar o estado de "Saindo..." quando a prop isAuthenticated mudar', async () => {
    let resolveLogout: (value?: unknown) => void = () => {};
    useAuthSpy.mockReturnValue({
      isAuthenticated: true,
      //@ts-expect-error - id é opcional, mas para o teste precisamos
      user: { id: "1", name: "User", role: "STUDENT" },
      logout: vi.fn().mockImplementation(() => {
        return new Promise((resolve) => {
          resolveLogout = resolve;
        });
      }),
    });

    const { rerender } = render(<Navbar />);

    const logoutBtn = screen.getByRole("button", { name: /sair da conta/i });
    fireEvent.click(logoutBtn);

    expect(logoutBtn).toHaveTextContent("Saindo...");

    //@ts-expect-error - id é opcional, mas para o teste precisamos
    useAuthSpy.mockReturnValue({
      isAuthenticated: false,
      user: null,
      logout: vi.fn(),
    });

    rerender(<Navbar />);

    useAuthSpy.mockReturnValue({
      isAuthenticated: true,
      //@ts-expect-error - id é opcional, mas para o teste precisamos
      user: { id: "1", name: "User", role: "STUDENT" },
      logout: vi.fn(),
    });

    rerender(<Navbar />);

    expect(
      screen.getByRole("button", { name: /sair da conta/i }),
    ).toHaveTextContent("Sair");

    if (resolveLogout) {
      resolveLogout();
    }
  });
});
