import { eventService } from "@/services/eventService";
import { EventCard } from "@/components/events/EventCard";
import { RoleGuard } from "@/components/auth/RoleGuard";
import { Event } from "@/types/event";

export default async function MyEventsPage() {
  let my_events: Event[] = []; 
  let error: string | null = null;

  try {
    my_events = await eventService.getMyEvents();
  } catch (err) {
    console.error("Erro na página meus eventos:", err);
    
    if (err instanceof Error) {
      error = err.message;
    } else {
      error = "Erro desconhecido ao carregar eventos.";
    }
  }

  return (
    <RoleGuard allowedRoles={["STUDENT", "PROFESSOR", "ORGANIZER", "ADMIN"]}>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-zinc-900 dark:text-white flex items-center gap-3">
            <span>⭐</span> Meus Eventos
          </h1>
          <p className="mt-2 text-zinc-600 dark:text-zinc-400">
            Acompanhe os eventos nos quais você realizou inscrição.
          </p>
        </div>

        {error && (
          <div className="bg-red-50 dark:bg-red-900/30 text-red-600 dark:text-red-400 p-4 rounded-lg mb-6 border border-red-200 dark:border-red-800">
            {error}
          </div>
        )}

        {!error && my_events.length === 0 ? (
          <div className="bg-white dark:bg-zinc-800 rounded-xl shadow-sm border border-zinc-200 dark:border-zinc-700 p-12 text-center">
            <span className="text-4xl block mb-4">📅</span>
            <h3 className="text-lg font-medium text-zinc-900 dark:text-white mb-2">
              Nenhuma inscrição encontrada
            </h3>
            <p className="text-zinc-500 dark:text-zinc-400">
              Você ainda não se inscreveu em nenhum evento. Navegue pela página de
              eventos para descobrir novas oportunidades!
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {my_events.map((event) => (
              <EventCard key={event.id} event={event} />
            ))}
          </div>
        )}
      </div>
    </RoleGuard>
  );
}