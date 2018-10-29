# PicoPiImx7dTemperature_ConcurrentThreads
Vengono presentate in seguito 2 varianti dell'applicazione PicoPiImx7dTemperature.

La prima versione, PicoPiImx7d_NOThreads, gestisce il rilevamento della temperatura, l'attivazione dell'allarme in caso di
superamento della soglia massima e il display con la stampa della temperatura rilevata senza l'ausilio di thread paralleli, ma 
attraverso un singolo thread suddiviso vari task asincroni.

La seconda versione, PicoPiImx7dTemperature_Threads, ripropone le stesse funzionalità, con la differenza che la gestione delle attività e affidata a più thread paralleli.
