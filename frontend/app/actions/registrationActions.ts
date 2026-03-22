"use server";

import { cookies } from "next/headers";
import { revalidatePath } from "next/cache";
import { API_URL } from "./configs";

interface RegistrationActionResult {
  status: string;
  message: string;
}

export async function registerForEventAction(eventId: string) {
  const cookieStore = await cookies();
  const token = cookieStore.get("token")?.value;

  if (!token) {
    throw new Error("Você precisa estar autenticado para se inscrever.");
  }

  const res = await fetch(`${API_URL}/registrations/${eventId}/register`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
  });

  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}));
    throw new Error(
      errorData.message || "Falha ao realizar inscrição no evento.",
    );
  }

  const data = (await res.json()) as RegistrationActionResult;

  revalidatePath(`/events/${eventId}`);
  revalidatePath(`/events/${eventId}/waiting-list`);
  revalidatePath("/events");

  return data;
}

export async function cancelRegistrationAction(eventId: string) {
  const cookieStore = await cookies();
  const token = cookieStore.get("token")?.value;

  if (!token) {
    throw new Error(
      "Você precisa estar autenticado para cancelar a inscrição.",
    );
  }

  const res = await fetch(`${API_URL}/registrations/${eventId}/cancel`, {
    method: "DELETE",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}));
    throw new Error(
      errorData.message || "Falha ao cancelar a inscrição no evento.",
    );
  }

  revalidatePath(`/events/${eventId}`);
  revalidatePath(`/events/${eventId}/waiting-list`);
  revalidatePath("/events");
}
