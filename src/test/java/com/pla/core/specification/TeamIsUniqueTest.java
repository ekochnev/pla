/*
 * Copyright (c) 3/5/15 4:20 PM .NthDimenzion,Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.pla.core.specification;

import com.pla.core.dto.TeamDto;
import com.pla.core.query.TeamFinder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @author: Nischitha
 * @since 1.0 18/03/2015
 */
@RunWith(MockitoJUnitRunner.class)
public class TeamIsUniqueTest {

    @Mock
    private TeamFinder teamFinder;


    @Test
    public void shouldReturnTrueWhenTeamNameUnique() {
        when(teamFinder.getTeamCountByTeamCode("Team1")).thenReturn(0);
        when(teamFinder.getTeamCountByTeamName("Team")).thenReturn(0);
        TeamIsUnique teamIsUnique = new TeamIsUnique(teamFinder);
        TeamDto teamDto = new TeamDto("Teamname", "TeamCode", "sdsd");
        boolean alreadyExists = teamIsUnique.isSatisfiedBy(teamDto);
        assertTrue(alreadyExists);
    }

   @Test
   public void shouldReturnFalseWhenTeamNameNotUnique() {
       when(teamFinder.getTeamCountByTeamCode("Team1")).thenReturn(1);
       when(teamFinder.getTeamCountByTeamName("Team")).thenReturn(1);
       TeamIsUnique teamIsUnique = new TeamIsUnique(teamFinder);
       TeamDto teamDto = new TeamDto("Team", "Team1", "232321");
       boolean alreadyExists = teamIsUnique.isSatisfiedBy(teamDto);
       assertFalse(alreadyExists);
    }
}
