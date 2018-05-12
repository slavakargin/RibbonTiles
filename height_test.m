h = load('height.txt')
x = h(:,1); y = h(:,2); v = h(:,3 : n + 2);
[xq,yq] = meshgrid(0:.05:max(x), 0:.05:max(y));
vq = griddata(x,y,v(:,1),xq,yq);
figure(1)
mesh(xq,yq,vq)
hold on
plot3(x,y,v(:,1),'.')
xlim([0 max(x)])
ylim([0 max(y)])
colorbar
xlabel('x')
ylabel('y')