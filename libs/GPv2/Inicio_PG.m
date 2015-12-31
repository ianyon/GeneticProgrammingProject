%load GPalta classes
% p = javaclasspath;
% if isempty(strfind(p,'build'))
%     javaaddpath({['build' filesep 'classes'],...
%         ['build' filesep 'classes' filesep 'gpalta' filesep 'core'],...
%         ['build' filesep 'classes' filesep 'gpalta' filesep 'multitree'],...
%         ['build' filesep 'classes' filesep 'gpalta' filesep 'nodes'],...
%         ['build' filesep 'classes' filesep 'gpalta' filesep 'ops']})
% end
p = javaclasspath;
if isempty(strfind(p,'GPv2.jar'))
    javaaddpath GPv2.jar
end
clear p
% if isempty(strfind(p,'/GPv2/dist/GPv2.jar'))
%     javaaddpath('/GPv2/dist/GPv2.jar')
% end
%  
%generate fitness cases

maxGen=10;
% examples=50;
% x(1,:)=10*rand(1,examples);
% x(2,:)=10*rand(1,examples);
% y = x(1,:).*x(1,:).*x(2,:) + x(1,:).*x(2,:) + x(2,:);
% disp ('Looking for x2*x1^2 + x2*x1 + x2')
t = -5*pi:0.5:5*pi;
examples = length(t);
X = cos(t);
Y = sin(t);
Z = (1/sqrt(2))*t;
x = [X;Y;Z];
y = [];
 
%initialize evolution:
tic; 
config = gpalta.core.Config('Config.txt');
evo = gpalta.core.Evolution(config, x, zeros(1,examples), zeros(1,examples), true);
evo.eval();
 
%go
for i=1:maxGen,
%    if 1 - evo.evoStats.bestSoFar.readFitness < .001
%        time = toc;
%        disp (['Objective reached in generation ' num2str(i) ' (' num2str(time) ' seconds)'])
%        break
%    end
   evo.evolve;
   evo.eval;
end
 
%evolution done (objective or max generations reached)
disp (['winner tree: ' char(evo.evoStats.bestSoFar.toString)])
diff = evo.getRawOutput(evo.evoStats.bestSoFar).x'-y;
disp (['fitness: ' num2str(evo.evoStats.bestSoFar.readFitness) ', MSE: ' num2str(sqrt(sum(diff.^2)))])

% evo.evoStats.bestSoFar(1).getTree(1)
% out=evo.getRawOutput(evo.evoStats.bestSoFar)
% out.getArray(0)