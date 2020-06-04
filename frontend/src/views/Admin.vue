<template>
  <div class="admin">
      <router-link :to="{name:'UserManagement'}" class="nav-link" v-if="$store.getters.userInfo.isAdmin" >{{ $t('user.management') }}</router-link>
      <router-link :to="{name:'CourseManagement'}" class="nav-link" >{{ $t('course.management') }}</router-link>
      <router-link :to="{name:'SemesterManagement'}" class="nav-link" v-if="$store.getters.userInfo.isAdmin" >{{ $t('semester.create') }}</router-link>
  </div>
</template>