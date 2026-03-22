import Link from "next/link";
import { ArrowLeft, Clock3, ListOrdered } from "lucide-react";
import { getWaitingListAction } from "@/app/actions/presenceActions";
import { RegistrationResponseDTO } from "@/types/event";

export default async function WaitingListPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  let waitingList: RegistrationResponseDTO[] = [];

  try {
    waitingList = await getWaitingListAction(id);
  } catch (error) {
    void error;
    return (
      <div className="max-w-4xl mx-auto p-8 text-center mt-20">
        <h2 className="text-2xl font-bold text-red-600 mb-4">Acesso Negado</h2>
        <p className="text-zinc-600 dark:text-zinc-400">
          Você não é o organizador deste evento ou o evento não existe.
        </p>
        <Link
          href="/events"
          className="text-blue-600 hover:underline mt-4 inline-block"
        >
          Voltar para eventos
        </Link>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-zinc-50 dark:bg-black py-8">
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
        <Link
          href={`/events/${id}`}
          className="inline-flex items-center text-sm text-zinc-600 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-white mb-6 transition-colors"
        >
          <ArrowLeft className="w-4 h-4 mr-2" />
          Voltar para os detalhes do evento
        </Link>

        <div className="mb-8">
          <h1 className="text-3xl font-bold text-zinc-900 dark:text-white mb-2 flex items-center gap-3">
            <ListOrdered className="w-7 h-7 text-violet-600 dark:text-violet-400" />
            Lista de Espera
          </h1>
          <p className="text-zinc-500 dark:text-zinc-400">
            A ordem abaixo respeita a fila de entrada dos interessados quando o
            evento está lotado.
          </p>
        </div>

        {waitingList.length === 0 ? (
          <div className="bg-white dark:bg-zinc-900 rounded-xl border border-zinc-200 dark:border-zinc-800 p-8 text-center">
            <Clock3 className="w-10 h-10 mx-auto text-zinc-400 mb-4" />
            <h2 className="text-xl font-semibold text-zinc-900 dark:text-white mb-2">
              Nenhuma pessoa na lista de espera
            </h2>
            <p className="text-zinc-500 dark:text-zinc-400">
              Quando o evento lotar, os novos interessados aparecerão aqui em
              ordem de entrada.
            </p>
          </div>
        ) : (
          <div className="bg-white dark:bg-zinc-900 rounded-xl border border-zinc-200 dark:border-zinc-800 overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="bg-zinc-50 dark:bg-zinc-800 border-b border-zinc-200 dark:border-zinc-700">
                  <tr>
                    <th className="p-4 w-24">Posição</th>
                    <th className="p-4">Nome</th>
                    <th className="p-4">E-mail</th>
                    <th className="p-4">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-zinc-200 dark:divide-zinc-800">
                  {waitingList.map((registration, index) => (
                    <tr
                      key={registration.userId}
                      className="hover:bg-zinc-50 dark:hover:bg-zinc-800/50 transition-colors"
                    >
                      <td className="p-4 font-semibold text-violet-700 dark:text-violet-300">
                        #{index + 1}
                      </td>
                      <td className="p-4 font-medium text-zinc-900 dark:text-zinc-100">
                        {registration.userName}
                      </td>
                      <td className="p-4 text-zinc-500 dark:text-zinc-400">
                        {registration.userEmail}
                      </td>
                      <td className="p-4">
                        <span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium bg-amber-100 text-amber-800 border border-amber-200 dark:bg-amber-900/30 dark:text-amber-300 dark:border-amber-800">
                          Lista de Espera
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
