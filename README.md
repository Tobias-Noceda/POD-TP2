# Viajes en Autos de Aplicacion en Nueva York

Trabajo Practico 2: Programacion de Objetos Distribuidos

## Compilacion

Se debe ejecutar build.sh. Este script automatiza la compilación del proyecto limpia, compilando y preparando todas las dependencias necesarias para despues correr el servidor y el cliente.

```bash
./build.sh
```

## Ejecucion

Para la ejecucion del sistema se requiere correr el servidor y el cliente.

### Servidor

Ejecutar el archivo `run-server.sh`, que se encuentra en `server/src/main/assembly/overlay/`
```bash
./run-server.sh
```

### Cliente

Para ejecutar cada una de las queries, se debe correr el archivo correspondiente. Los mismos se encuentran en `client/src/main/assembly/overlay`.

El formato para ejecutar es el siguiente
```bash
./queryX.sh -Daddresses='xx.xx.xx.xx:XXXX;yy.yy.yy.yy:YYYY' -DinPath=XX -DoutPath=YY [params]
```

Todos usan los siguientes parametros
- `address`: Direcciones IP de los nodos con sus puertos (una o más, separadas por punto y coma) 
- `inPath`: Path donde están los archivos de entrada de viajes y zonas
- `outPath`: Path donde se crearán los archivos de salida
- `[params]`: Parametros extras que corresponden para algunas queries

#### Query 1

Total de viajes por zona de inicio y zona de finalización

```bash
./query1.sh -Daddresses='xx.xx.xx.xx:XXXX;yy.yy.yy.yy:YYYY' -DinPath=XX -DoutPath=YY
```

#### Query 2

Viaje más largo dentro de la ciudad por zona de inicio

```bash
./query2.sh -Daddresses='xx.xx.xx.xx:XXXX;yy.yy.yy.yy:YYYY' -DinPath=XX -DoutPath=YY
```

#### Query 3

Precio promedio de viaje por barrio de inicio y compañía

```bash
./query3.sh -Daddresses='xx.xx.xx.xx:XXXX;yy.yy.yy.yy:YYYY' -DinPath=XX -DoutPath=YY
```

#### Query 4

Viaje con mayor demora (en segundos) por zona de inicio para un barrio

Parametro adicional: `borough`

```bash
./query4.sh -Daddresses='xx.xx.xx.xx:XXXX;yy.yy.yy.yy:YYYY' -DinPath=XX -DoutPath=YY -Dborough='ABC'
```

#### Query 5

Total de millas Year to Date por compañía

```bash
./query5.sh -Daddresses='xx.xx.xx.xx:XXXX;yy.yy.yy.yy:YYYY' -DinPath=XX -DoutPath=YY
```


### Ejemplos

#### Query 1

```bash
./query1.sh -Daddresses='10.6.0.1:5701' -DinPath=. -DoutPath=.
```

#### Query 2

```bash
./query2.sh -Daddresses='10.6.0.1:5701' -DinPath=. -DoutPath=.
```

#### Query 3

```bash
./query3.sh -Daddresses='10.6.0.1:5701' -DinPath=. -DoutPath=.
```

#### Query 4

```bash
./query4.sh -Daddresses='10.6.0.1:5701' -DinPath=. -DoutPath=. -Dborough=Manhattan
```

#### Query 5

```bash
./query5.sh -Daddresses='10.6.0.1:5701' -DinPath=. -DoutPath=.
```