service Main {

    outputPort output{
        oneWay:
            f1(any), f2(any), f3(any)
    }


    main {
        for (elem1 in array1){
            f1@output(void)
            for (elem2 in array2){
                f2@output(void)
            }
            f3@output(void)
        }
    }
}
