# PicoPiImx7dTemperature_ConcurrentThreads
Two variants of the PicoPiImx7dTemperature application are presented below.

The first version, PicoPiImx7d_NOThreads, manages the temperature detection and the activation of the alarm in case of
exceeding the maximum threshold and prints in the display the temperature detected without the aid of parallel threads, but
through a single thread divided several asynchronous tasks.

The second version, PicoPiImx7dTemperature_Threads, proposes the same functionality, with the difference that task management is entrusted to multiple parallel threads.
