from console import Console

interface ExampleFaultInt {
    requestResponse:
        op(any)(any) throws InvalidNumberFormat(string)
}

service Main {
    embed Console as Console

    main {
        install( InvalidNumberFormat => println@Console( InvalidNumberFormat )() )
        throw InvalidNumberFormat("ciao")
    }
}