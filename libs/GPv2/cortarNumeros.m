function out = cortarNumeros(string, n)

if isa(string,'gpalta.core.Tree')
    string = char(string.toString);
end

if ~exist('n')
    n=1;
end

out = regexprep (string, ['(\d+\.\d{' num2str(n) '})\d+'], '$1');