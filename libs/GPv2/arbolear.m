function [arbol string]=arbolear(string)

if isa(string,'gpalta.core.Individual') || isa(string,'gpalta.core.Tree')
    string = char(string.toString);
end

%siempre empezamos con un (, luego viene una palabra:
[inicio fin]=regexp(string,'[a-zA-Z_0-9\.]+');
arbol.op=string(inicio(1):fin(1));
string=string(fin(1)+2:end);

while ~isempty(string)
    inicio=regexp(string,'\s*(');
    if isempty(inicio)
        inicio(1)=-1;
    end
    if inicio(1)==1        
        campos=fieldnames(arbol);
        no_tiene_kids=1;
        for i=1:length(campos)
            if strcmp(campos(i),'kids')
                no_tiene_kids=0;
            end
        end
        if no_tiene_kids
            [arbol.kids{1} string]=arbolear(string);
        else
            [arbol.kids{length(arbol.kids)+1} string]=arbolear(string);
        end
    else
        fin=regexp(string,')');
        if isempty(fin)
            fin(1)=-1;
        end
        %si empieza con ), terminamos
        if fin(1)==1
            string=string(fin(1)+1:end);
            break
        %si no empieza con )
        else
            [inicio fin]=regexp(string,'[a-zA-Z_0-9\.\-]+');
            campos=fieldnames(arbol);
            no_tiene_kids=1;
            for i=1:length(campos)
                if strcmp(campos(i),'kids')
                    no_tiene_kids=0;
                end
            end
            if no_tiene_kids
                arbol.kids{1}.op=string(inicio(1):fin(1));
                arbol.kids{1}.kids=[];
            else
                arbol.kids{length(arbol.kids)+1}.op=string(inicio(1):fin(1));
                arbol.kids{length(arbol.kids)}.kids=[];
            end
            string=string(fin(1)+1:end);
        end
    end
end
