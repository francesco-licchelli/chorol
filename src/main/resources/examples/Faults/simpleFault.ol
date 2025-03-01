service Main {
    outputPort output{
        oneWay:
            f1(void)
    }

    main {
        install( SampleFault => f1@output(void))
        throw SampleFault(void)
    }
}