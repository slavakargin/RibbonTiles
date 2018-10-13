Nmax = 50;

global AA BB CC;

AA = zeros(Nmax + 1, 1); % list of numbers of tilings of the 3-by-N strip
BB = zeros(Nmax + 1, 1); % list of numbers of tilings of the strip without vertical tiles
CC = zeros(Nmax + 1, 1); % list of numbers of tilings of the porch staircase region 
                     % (without vertical tiles)
AA(1,1) = 1; %corresponds to N = 0
AA(2,1) = 1;
BB(1,1) = 1;
BB(2,1) = 0;
CC(1,1) = 1;
CC(2,1) = 1;
CC(3,1) = 1;

entropy = zeros(Nmax,1);
for N = 1 : Nmax
    Alpha(N);
    Beta(N);
    Camma(N);
    entropy(N) = log2(AA(N + 1,1))/N;
end
AA(1:20)
BB(1:20)
CC(1:20)

%characteristic polynomial
P = [1, 0, 0, -4, -1, -1, 1];
rts = roots(P);
mu = - log2(rts(6))
plot(1:Nmax, entropy, 1:Nmax, mu * ones(Nmax,1));