# Cmm launguage 
### Part four : semantic analysis

With the help of ANTLR 4 tool and Java programming language.

At the end of this section, the compiler is fully implemented and converts programs written in --C to machine-executable code. The implementation must generate the equivalent bytecode for each input file in --C language. In the tests of this section, only the ability to generate your compiler code is measured and the inputs do not have syntactic and semantic errors that you examined in the previous sections; Note, however, that you need the information collected in the sign table and the AST node type information to generate the code.

Sample code for this language:

```
struct Person begin
 int age;
 int weight;
 int id;
end
void print_id(list# struct Person person) begin
 int i
 i = 0
 do begin
 display (person[i].id)
 i = i + 1
 end
 while i < n 
end
 
main() begin
 int i, n = 10;
 list #struct Person people;
 while ~ (i == n) begin
 struct Person new_person
 new_person.id = i
 append(people, new_person)
 i = i + 1
 end
 fptr <list #struct Person -> void> pointer = print_id
 pointer(people)
end

```

Other parts:
- [lexical and syntactic analyzer](https://github.com/Perriex/Cmm-lexical-and-syntactic-analyzer)
- [symbol table](https://github.com/Perriex/Cmm-symbol-table)
- [semantic analysis](https://github.com/Perriex/Cmm-semantic-analysis)
- [compile and run](https://github.com/Perriex/Cmm-compile)

