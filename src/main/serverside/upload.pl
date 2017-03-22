#!/usr/bin/perl

use 5.1.0;
use CGI;
use DBI;
use DBD::mysql;

print qq(Content-type: text/plain\n);
print qq(\n);

my $dbh=DBI->connect("DBI:mysql:database=emcmarket;host=localhost", "emcmarket", "NE6xYRe6UPwW3Ray", {AutoCommit => 0 });

my $cgi=CGI->new;
my $rows=0;
if ($cgi->param("upload")) {
	if ($cgi->param("clientversion") eq "") {
	} else {
		my $name=$cgi->param("name");
		my $separator=":";
		if ($cgi->param("clientversion") ne "") {
			$separator="\\|";
		}
		my $sthdel=$dbh->prepare(qq(
			delete from signs
			where server=? and x=? and y=? and z=? and chooseposition=?
		));
		my $sthins=$dbh->prepare(qq(
			insert into signs (server, x, y, z, amount, buy, sell, owner, item, resnumber, chooseposition, lastseen, uploader, todelete)
			values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
		));
		foreach my $row (split("\n", $cgi->param("upload"))) {
			my ($server, $x, $y, $z, $amount, $buy, $sell, $owner, $item, $resnumber, $choosepos, $lastseen, $todelete)=
				split($separator, $row);
			$choosepos="0" if $choosepos eq "";
			$lastseen=time*1000 if $lastseen eq "";
			if ($todelete eq "todelete") { $todelete = 1; }
			else { $todelete=0; }
			$sthdel->execute($server, $x, $y, $z, $choosepos);
			$sthins->execute($server, $x, $y, $z, $amount, $buy, $sell, $owner, $item, $resnumber, $choosepos, $lastseen, $name, $todelete) or print "$row : ".$dbh->errstr."\n";
			$rows++;
		}
		$dbh->commit;
	}
	open(F, ">>/tmp/uploaders.dat");
	print F scalar localtime, "  ",  $rows ," uploaded by ", $cgi->param("name"), " client ", $cgi->param("clientversion"), "\n";
	close F;
}

print "$rows rows uploaded.\n";
