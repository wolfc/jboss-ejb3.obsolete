EXCLUDES=--exclude '.svn' --exclude 'Makefile'
# --delete-excluded

distr:	slave3
	sudo -u hudson rsync -av ../common ~hudson/ $(EXCLUDES)
	sudo -u hudson rsync -av ../common hudson@192.168.100.11:. $(EXCLUDES)
	sudo -u hudson rsync -av ../common hudson@192.168.100.12:. $(EXCLUDES)

slave3:
	sudo -u hudson rsync -av --rsync-path="ssh 192.168.100.11 rsync" ../common hudson@sulley:. $(EXCLUDES)
