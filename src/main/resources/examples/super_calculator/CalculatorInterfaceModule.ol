/*
//Interfaccia per la calcolatrice NON tipata
interface CalculatorInterface {
    RequestResponse:
        sum,
        sub,
        mul,
        div
}
*/

//Interfaccia tipata

type SumRequest: void {
    term[1,*]: int //un array con int >= 1
}

type SubRequest: void {
    minuend: int
    subtracted: int
}

type MulRequest: void {
    factor*: double //un array con 0+ double
}
type DivRequest: void {
    dividend: double
    divisor: double
}

type Prova: void {
    ciao: DivRequest
}


type test: bool

type testChoice: int | DivRequest | we | dibwbi

interface CalculatorInterface {
    RequestResponse:
        sum( SumRequest )( int ) throws NumberException( SubRequest ),
        sub( SubRequest )( int ) throws fault1 fault2 fault3,
        //bug1 e metto ciao, il typeholder mi trova ciao dichiarato in Prova, che e' fuori dallo scope
        mul( MulRequest )( double ),
        div( DivRequest )( double ),
        shutdown( void )( void ),

    OneWay:
        ipab( ciao ),
        ciao
}