import { Calendar, Clock, MapPin, Users } from "lucide-react";
import Link from "next/link";
import { Event, EventStatus } from "@/types/event";
import { JSX } from "react";

interface EventCardProps {
  event: Event;
}

export function EventCard({ event }: Readonly<EventCardProps>) {
  const spotsLeft = Math.max(event.capacity - event.registered, 0);
  const isFewSpots = spotsLeft < 20 && spotsLeft > 0;

  const dateObj = new Date(event.date + "T00:00:00");
  const formattedDate = dateObj.toLocaleDateString("pt-BR", {
    weekday: "long",
    month: "long",
    day: "numeric",
    year: "numeric",
  });

  const now = new Date();

  const todayStr = new Intl.DateTimeFormat("en-CA", {
    timeZone: "America/Sao_Paulo",
  }).format(now);

  const currentTimeStr = new Intl.DateTimeFormat("en-GB", {
    timeZone: "America/Sao_Paulo",
    hour: "2-digit",
    minute: "2-digit",
  }).format(now);

  const isPastDate = event.date < todayStr;
  const isPastTime = event.date === todayStr && event.endTime < currentTimeStr;
  const isPast = isPastDate || isPastTime;
  const isWaitingList = event.userRegistrationStatus === "WAITING_LIST";
  const isConfirmedRegistration = event.isRegistered && !isWaitingList;
  const isSoldOut = !isPast && spotsLeft === 0 && !event.isRegistered;

  const getRegistrationBadge = (): JSX.Element | null => {
    const styles = {
      cancelled:
        "px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800 border border-red-200 dark:bg-red-900/30 dark:text-red-300 dark:border-red-800 ml-2",
      past: "px-2.5 py-0.5 rounded-full text-xs font-medium bg-zinc-100 text-zinc-500 border border-zinc-200 dark:bg-zinc-800 dark:text-zinc-400 dark:border-zinc-700 ml-2",
      registered:
        "px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800 border border-green-200 dark:bg-green-900/30 dark:text-green-300 dark:border-green-800 ml-2",
      soldOut:
        "px-2.5 py-0.5 rounded-full text-xs font-medium bg-rose-100 text-rose-800 border border-rose-200 dark:bg-rose-900/30 dark:text-rose-300 dark:border-rose-800 ml-2",
      waitingList:
        "px-2.5 py-0.5 rounded-full text-xs font-medium bg-amber-100 text-amber-800 border border-amber-200 dark:bg-amber-900/30 dark:text-amber-300 dark:border-amber-800 ml-2",
    };

    if (event.status === EventStatus.CANCELLED) {
      return <span className={styles.cancelled}>Cancelado</span>;
    }

    if (!isPast && !event.isRegistered && !isSoldOut) {
      return null;
    }

    return (
      <>
        {isPast && <span className={styles.past}>Evento já realizado</span>}
        {isSoldOut && <span className={styles.soldOut}>Esgotado</span>}
        {isWaitingList && (
          <span className={styles.waitingList}>Na fila de espera</span>
        )}
        {isConfirmedRegistration && (
          <span className={styles.registered}>Inscrito</span>
        )}
      </>
    );
  };

  const getCategoryColor = (category: string) => {
    switch (category) {
      case "palestra":
        return "bg-blue-100 text-blue-800 border-blue-200 dark:bg-blue-900/30 dark:text-blue-300 dark:border-blue-800";
      case "seminario":
        return "bg-lime-100 text-lime-800 border-lime-200 dark:bg-lime-900/30 dark:text-lime-300 dark:border-lime-800";
      case "cultural":
        return "bg-purple-100 text-purple-800 border-purple-200 dark:bg-purple-900/30 dark:text-purple-300 dark:border-purple-800";
      case "feira":
        return "bg-fuchsia-100 text-fuchsia-800 border-fuchsia-200 dark:bg-fuchsia-900/30 dark:text-fuchsia-300 dark:border-fuchsia-800";
      case "workshop":
        return "bg-pink-100 text-pink-800 border-pink-200 dark:bg-pink-900/30 dark:text-pink-300 dark:border-pink-800";
      case "livre":
        return "bg-cyan-100 text-cyan-800 border-cyan-200 dark:bg-cyan-900/30 dark:text-cyan-300 dark:border-cyan-800";
      case "conferencia":
        return "bg-orange-100 text-orange-800 border-orange-200 dark:bg-orange-900/30 dark:text-orange-300 dark:border-orange-800";
      case "festival":
        return "bg-rose-100 text-rose-800 border-rose-200 dark:bg-rose-900/30 dark:text-rose-300 dark:border-rose-800";
      case "outro":
      default:
        return "bg-gray-100 text-gray-800 border-gray-200 dark:bg-gray-800 dark:text-gray-300 dark:border-gray-700";
    }
  };

  return (
    <div className="group flex flex-col h-full bg-white dark:bg-zinc-900 rounded-xl border border-zinc-200 dark:border-zinc-800 shadow-sm hover:shadow-md transition-shadow duration-300 overflow-hidden">
      <div className="p-6 flex flex-col h-full">
        <div className="flex flex-wrap gap-2 mb-4">
          <span
            className={`px-2.5 py-0.5 rounded-full text-xs font-medium border capitalize ${getCategoryColor(event.category)}`}
          >
            {event.category}
          </span>
          {getRegistrationBadge()}
          {isFewSpots && !isPast && (
            <span className="px-2.5 py-0.5 rounded-full text-xs font-medium bg-amber-100 text-amber-800 border border-amber-200 dark:bg-amber-900/30 dark:text-amber-300 dark:border-amber-800">
              Poucas vagas
            </span>
          )}
        </div>

        <h3 className="text-lg font-bold text-zinc-900 dark:text-white mb-2 line-clamp-2 group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors">
          {event.title}
        </h3>
        <p className="text-sm text-zinc-600 dark:text-zinc-400 mb-6 line-clamp-3 flex-1">
          {event.description}
        </p>

        <div className="space-y-2.5 text-sm text-zinc-600 dark:text-zinc-400 mb-6">
          <div className="flex items-center gap-2.5">
            <Calendar className="w-4 h-4 text-zinc-400" />
            <span>{formattedDate}</span>
          </div>
          <div className="flex items-center gap-2.5">
            <Clock className="w-4 h-4 text-zinc-400" />
            <span>
              {event.startTime} - {event.endTime}
            </span>
          </div>
          <div className="flex items-center gap-2.5">
            <MapPin className="w-4 h-4 text-zinc-400" />
            <span className="line-clamp-1">{event.location}</span>
          </div>
          <div className="flex items-center gap-2.5">
            <Users className="w-4 h-4 text-zinc-400" />
            <span>
              {event.registered} / {event.capacity} inscritos
              <span className="text-zinc-400 ml-1">
                ({spotsLeft} restantes)
              </span>
            </span>
          </div>
        </div>

        <Link
          href={`/events/${event.id}`}
          className="w-full mt-auto py-2.5 px-4 bg-zinc-900 hover:bg-zinc-800 dark:bg-white dark:hover:bg-zinc-200 text-white dark:text-black text-sm font-medium rounded-lg transition-colors text-center block"
        >
          Ver Detalhes
        </Link>
      </div>
    </div>
  );
}
