"use client";

import { useState } from "react";
import { useAuth } from "@/contexts/AuthContext";
import {
  registerForEventAction,
  cancelRegistrationAction,
} from "@/app/actions/registrationActions";
import Link from "next/link";
import { ListOrdered } from "lucide-react";
import { OrganizerResponseDTO } from "@/types/organizer";

interface EventRegistrationButtonProps {
  eventId: string;
  isRegistered: boolean;
  userRegistrationStatus: string;
  organizerEmail: string;
  isCanceled: boolean;
  isPast: boolean;
  isFull: boolean;
  isCompleted: boolean;
  organizers: OrganizerResponseDTO[];
}

export function EventRegistrationButton({
  eventId,
  isRegistered,
  userRegistrationStatus,
  organizerEmail,
  isCanceled,
  isPast,
  isFull,
  isCompleted,
  organizers,
}: Readonly<EventRegistrationButtonProps>) {
  const [isLoading, setIsLoading] = useState(false);
  const { isAuthenticated } = useAuth();
  const isWaitingList = userRegistrationStatus === "WAITING_LIST";

  const isOrganizer = organizers.some(
    (org) => org.contactEmail === organizerEmail,
  );

  // O organizador não pode se inscrever no próprio evento
  if (isAuthenticated && isOrganizer) {
    return (
      <div className="flex flex-col gap-3">
        <div className="w-full py-3 px-4 bg-zinc-100 dark:bg-zinc-800 text-zinc-500 dark:text-zinc-400 font-medium rounded-lg text-center border border-zinc-200 dark:border-zinc-700">
          Você é o organizador deste evento
        </div>

        {isCanceled ? (
          <button
            disabled
            className="w-full py-3 px-4 bg-red-100 text-red-600 dark:bg-red-900/30 dark:text-red-400 rounded-md font-medium cursor-not-allowed"
          >
            Evento Cancelado
          </button>
        ) : (
          <>
            <Link
              href={`/events/${eventId}/presence`}
              className="w-full py-3 px-4 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg text-center transition-colors shadow-lg shadow-blue-600/20"
            >
              📋 Gerenciar Presenças
            </Link>
            <Link
              href={`/events/${eventId}/waiting-list`}
              className="w-full py-3 px-4 bg-white dark:bg-zinc-900 text-zinc-900 dark:text-zinc-100 font-medium rounded-lg text-center border border-zinc-200 dark:border-zinc-700 hover:bg-zinc-50 dark:hover:bg-zinc-800 transition-colors flex items-center justify-center gap-2"
            >
              <ListOrdered className="w-4 h-4" />
              Ver Lista de Espera
            </Link>
          </>
        )}
      </div>
    );
  }

  const handleAction = async () => {
    try {
      if (isRegistered) {
        const shouldCancel = window.confirm(
          isWaitingList
            ? "Deseja sair da lista de espera deste evento?"
            : "Deseja cancelar sua inscrição neste evento?",
        );

        if (!shouldCancel) {
          return;
        }
      }

      setIsLoading(true);

      if (isRegistered) {
        await cancelRegistrationAction(eventId);
        alert(
          isWaitingList
            ? "Você saiu da lista de espera com sucesso."
            : "Inscrição cancelada com sucesso! A vaga foi liberada.",
        );
      } else {
        const result = await registerForEventAction(eventId);
        alert(result.message);
      }
    } catch (error) {
      alert(
        (error as Error).message ||
          "Ocorreu um erro ao processar sua solicitação.",
      );
    } finally {
      setIsLoading(false);
    }
  };

  if (isCanceled) {
    return (
      <button
        disabled
        className="w-full py-3 px-4 bg-red-100 text-red-600 dark:bg-red-900/30 dark:text-red-400 rounded-md font-medium cursor-not-allowed"
      >
        Evento Cancelado
      </button>
    );
  }

  if (isCompleted) {
    return (
      <button
        disabled
        className="w-full py-3 px-4 bg-blue-100 text-blue-600 dark:bg-blue-900/30 dark:text-blue-400 rounded-md font-medium cursor-not-allowed"
      >
        Evento Finalizado
      </button>
    );
  }

  if (isPast) {
    return (
      <button
        disabled
        className="w-full py-3 px-4 bg-zinc-200 text-zinc-500 dark:bg-zinc-800 dark:text-zinc-500 rounded-md font-medium cursor-not-allowed"
      >
        Inscrições Encerradas
      </button>
    );
  }

  if (isRegistered) {
    return (
      <button
        onClick={handleAction}
        disabled={isLoading}
        className="w-full py-3 px-4 bg-white dark:bg-zinc-900 text-red-600 border border-zinc-200 dark:border-zinc-700 font-medium rounded-lg hover:bg-red-50 dark:hover:bg-red-900/10 transition-colors disabled:opacity-50"
      >
        {isLoading
          ? "Processando..."
          : isWaitingList
            ? "Sair da Lista de Espera"
            : "Cancelar Inscrição"}
      </button>
    );
  }

  return (
    <button
      onClick={handleAction}
      disabled={isLoading}
      className="w-full py-3 px-4 bg-zinc-900 dark:bg-white text-white dark:text-black font-medium rounded-lg hover:bg-zinc-800 dark:hover:bg-zinc-200 transition-colors shadow-lg shadow-zinc-900/10 disabled:opacity-50"
    >
      {isLoading
        ? "Processando..."
        : isFull
          ? "Entrar na Lista de Espera"
          : "Inscrever-se no Evento"}
    </button>
  );
}
