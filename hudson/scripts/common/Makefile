EXCLUDES=--exclude '.svn' --exclude 'Makefile'
# --delete-excluded

distr:
	sudo -u hudson rsync -av ../common ~hudson/ $(EXCLUDES)
	sudo -u hudson rsync -av ../common hudson@192.168.100.11:. $(EXCLUDES)
	sudo -u hudson rsync -av ../common hudson@192.168.100.12:. $(EXCLUDES)
