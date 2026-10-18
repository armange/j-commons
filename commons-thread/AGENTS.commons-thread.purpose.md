# Propósito de commons-thread

## Escopo

Esta norma orienta decisões sobre as capacidades e a API pública do módulo
`commons-thread`, inclusive sua documentação e seus testes. Ela complementa as
instruções da raiz do projeto e as normas técnicas aplicáveis a cada arquivo.

`commons-thread` é uma biblioteca Java para facilitar execução, agendamento,
timeouts, tratamento de falhas e acompanhamento de tarefas. Sua implementação
nasceu no contexto do JDK 8, mas seu valor não depende de reproduzir APIs
antigas. A biblioteca não cria recursos
novos no JDK: facilita o acesso aos recursos disponíveis na versão mínima de
Java suportada pelo módulo.

## Critérios de produto

- Antes de criar, manter ou substituir uma facilidade, compare o uso da
  biblioteca com o uso direto do JDK suportado. Considere ao menos um caso
  simples e outro que exija configuração fornecida pelo consumidor.
- Mantenha uma API própria somente quando ela reduzir código ou complexidade
  para um caso real sem introduzir custos injustificados, restrições ou
  comportamentos ocultos.
  Uma chamada direta ao JDK é o destino preferido quando for a solução melhor.
- Não preserve uma facilidade apenas por existir na versão anterior. Ela pode
  ser removida quando o JDK oferecer alternativa melhor; registre a mudança do
  contrato publicado e a forma de migração conforme as normas do projeto.
- Permita que o consumidor use as opções nativas relevantes para a capacidade
  oferecida, como `ExecutorService`, `ScheduledExecutorService` e
  `ThreadFactory`, sem impor um tipo de thread, executor ou política de
  agendamento quando a escolha puder pertencer ao consumidor.
- Torne explícitos os efeitos de iniciar, aguardar e cancelar uma tarefa.
  Diferencie conclusão do resultado, cancelamento da tarefa e interrupção da
  thread; não apresente um deles como garantia dos demais.
- Faça com que falhas e resultados de operações assíncronas possam ser
  observados. Não esconda exceções da tarefa, de callbacks ou do fechamento de
  recursos atrás de uma interface aparentemente bem-sucedida.
- Defina quem possui e encerra executores e recursos. Uma facilidade não deve
  criar threads ou executores duradouros sem um ciclo de vida compreensível para
  o consumidor, nem encerrar recursos que ele forneceu sem contrato explícito.
- Prefira os tipos e contratos de concorrência do JDK quando expressarem o
  comportamento necessário. Uma abstração própria deve acrescentar uma
  conveniência verificável e preservar a possibilidade de customização nativa.

## Limites

Esta norma define critérios permanentes para o módulo. Planos de migração,
remoções específicas e decisões dependentes de uma versão particular do JDK
devem permanecer nos documentos de planejamento correspondentes.
