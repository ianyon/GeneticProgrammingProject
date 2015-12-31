Para comenzar a utilizar el código, diríjanse a la carpeta:
\matlab code\GPlab\FfrictionMSEreg01
Ejecuten el archivo:

q_pg_ffriction.m.

Los parámetros de entrada son:
	- indiv: número de individuos en la población
	- g: cantidad de generaciones
	- var: codificación de los parámetros del algoritmo.

Para iniciar una prueba solo ingresen los parámetros indiv y g de la siguiente forma:
% 10 individuos, 5 generaciones
[v,b] = q_pg_ffriction(10,5);

Enlace: https://www.dropbox.com/sh/t5f3vd9aita1pta/AADvN7X_Sewt94DqcRud4A2na?dl=0

GPalta
La carpeta src contiene todos los archivos fuentes y la carpeta docs contiene una breve
explicación de cada una de las funciones de GPalta. En la carpeta:
\GPv2\src\gpalta\core
están las funciones de fitness que utiliza el toolbox. En este toolbox hay varias funciones fitness (CCA, Samon,
Classic) las cuales puedes revisar y ver cómo llaman y manejan los parámetros internamente. Fíjate que la función de
Sammon trabaja con multi-arboles (la función CCA también), así es que considero que con estas funciones puedes
trabajar para mezclar las salidas de distintos árboles.